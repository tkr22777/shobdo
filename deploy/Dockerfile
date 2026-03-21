# Combined Docker image: Play Java backend + React/Nginx frontend
# Build context must be repo root: docker build -f deploy/Dockerfile .

# ── Stage 1: Build Play/Java backend ─────────────────────────────────────────
FROM eclipse-temurin:8-jdk AS java-build

RUN apt-get update && apt-get install -y --no-install-recommends bash curl unzip && \
    curl -L -o sbt.tgz https://github.com/sbt/sbt/releases/download/v0.13.18/sbt-0.13.18.tgz && \
    mkdir -p /opt/sbt && tar -xf sbt.tgz -C /opt/sbt --strip-components=1 && \
    ln -s /opt/sbt/bin/sbt /usr/bin/sbt && rm sbt.tgz

WORKDIR /app

# Dependency layer — cached unless build.sbt or project/ changes
COPY application/shobdo-app/build.sbt /app/
COPY application/shobdo-app/project /app/project/
RUN sbt update

COPY application/shobdo-app/ /app/
RUN sbt dist

# ── Stage 2: Build React frontend ────────────────────────────────────────────
FROM node:20-alpine AS node-build

WORKDIR /app

COPY client/react/package*.json ./
RUN npm ci

COPY client/react/ ./

ARG VITE_GOOGLE_CLIENT_ID
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID

RUN npm run build

# ── Stage 3: Combined runtime ─────────────────────────────────────────────────
FROM eclipse-temurin:8-jre

RUN apt-get update && \
    apt-get install -y --no-install-recommends nginx supervisor bash unzip && \
    rm -rf /var/lib/apt/lists/*

# Play app — unzip and settle at /shobdo-app/
COPY --from=java-build /app/target/universal/shobdo-app-0.11-SNAPSHOT.zip /shobdo-app.zip
RUN unzip /shobdo-app.zip -d /shobdo-app-dist && \
    mv /shobdo-app-dist/shobdo-app-0.11-SNAPSHOT /shobdo-app && \
    chmod +x /shobdo-app/bin/shobdo-app && \
    rm /shobdo-app.zip && \
    mkdir -p /shobdo-app/logs && chmod 777 /shobdo-app/logs

# React dist
COPY --from=node-build /app/dist /usr/share/nginx/html

# Configs
COPY deploy/nginx_combined.conf /etc/nginx/nginx.conf
COPY deploy/supervisord.conf /etc/supervisor/conf.d/shobdo.conf

ENV LOG4J_FORMAT_MSG_NO_LOOKUPS=true

EXPOSE 80

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]

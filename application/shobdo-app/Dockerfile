########################################################
# Dockerfile to build and run the dictionary app
########################################################

# Build argument to control whether to build from source or use pre-built zip
ARG BUILD_FROM_SOURCE=true

# Build stage
FROM eclipse-temurin:8-jdk AS build

# Set build argument in this stage
ARG BUILD_FROM_SOURCE

# Install SBT and other build dependencies if building from source
RUN if [ "$BUILD_FROM_SOURCE" = "true" ] ; then \
        apt-get update && apt-get install -y --no-install-recommends bash curl && \
        curl -L -o sbt.tgz https://github.com/sbt/sbt/releases/download/v0.13.18/sbt-0.13.18.tgz && \
        mkdir -p /opt/sbt && \
        tar -xf sbt.tgz -C /opt/sbt --strip-components=1 && \
        ln -s /opt/sbt/bin/sbt /usr/bin/sbt && \
        rm sbt.tgz ; \
    fi

# Set working directory
WORKDIR /app

# First, copy only the dependency-related files
# This creates a separate layer that will be cached unless these files change
COPY build.sbt /app/
COPY project /app/project/

# Pre-fetch dependencies if building from source
RUN if [ "$BUILD_FROM_SOURCE" = "true" ] ; then \
        sbt update ; \
    fi

# Now copy the rest of the project files
COPY . /app/

# Run SBT clean and dist if building from source
RUN if [ "$BUILD_FROM_SOURCE" = "true" ] ; then \
        sbt clean dist ; \
    else \
        echo "Skipping build, using pre-built zip file" ; \
    fi

# Runtime stage 
FROM eclipse-temurin:8-jre

# Install necessary runtime dependencies
RUN apt-get update && apt-get install -y --no-install-recommends bash unzip && rm -rf /var/lib/apt/lists/*

# Pass the build argument to this stage
ARG BUILD_FROM_SOURCE

# Create directory for the application
RUN mkdir -p /shobdo-app/bin/
# Create logs directory explicitly with proper permissions
RUN mkdir -p /shobdo-app/logs && chmod 777 /shobdo-app/logs

# Only copy the prebuilt zip if NOT building from source
COPY --from=build /app/target/universal/shobdo-app-0.11-SNAPSHOT.zip /shobdo-app/shobdo-app.zip

# Unzip the application
RUN unzip /shobdo-app/shobdo-app.zip -d /shobdo-app/bin/ && \
    chmod +x /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT/bin/shobdo-app

EXPOSE 9000

# Set working directory to ensure relative paths for logs work correctly
WORKDIR /shobdo-app

# Create volume for logs to persist outside container
VOLUME ["/shobdo-app/logs"]

# Set environment variables for logging
ENV LOG4J_FORMAT_MSG_NO_LOOKUPS=true

# Run with the correct configuration for Play framework
CMD /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT/bin/shobdo-app -Dplay.crypto.secret=testtahsin

########################################################
# Dockerfile to build and run the dictionary app
########################################################

# Build argument to control whether to build from source or use pre-built zip
ARG BUILD_FROM_SOURCE=true

# Build stage
FROM openjdk:8-jdk-alpine AS build

# Set build argument in this stage
ARG BUILD_FROM_SOURCE

# Install SBT and other build dependencies if building from source
RUN if [ "$BUILD_FROM_SOURCE" = "true" ] ; then \
        apk add --no-cache bash curl && \
        curl -L -o sbt.tgz https://github.com/sbt/sbt/releases/download/v0.13.18/sbt-0.13.18.tgz && \
        mkdir -p /opt/sbt && \
        tar -xf sbt.tgz -C /opt/sbt --strip-components=1 && \
        ln -s /opt/sbt/bin/sbt /usr/bin/sbt && \
        rm sbt.tgz ; \
    fi

# Set working directory
WORKDIR /app

# Copy project files if building from source
COPY . /app/

# Run SBT clean and dist if building from source
RUN if [ "$BUILD_FROM_SOURCE" = "true" ] ; then \
        sbt clean dist ; \
    else \
        echo "Skipping build, using pre-built zip file" ; \
    fi

# Runtime stage 
FROM openjdk:8-jre-alpine

# Install necessary runtime dependencies
RUN apk add --no-cache bash unzip

# Pass the build argument to this stage
ARG BUILD_FROM_SOURCE

# Create directory for the application
RUN mkdir -p /shobdo-app/bin/

# Copy the application zip file based on build mode
COPY target/universal/shobdo-app-0.11-SNAPSHOT.zip /shobdo-app/prebuilt.zip
COPY --from=build /app/target/universal/shobdo-app-0.11-SNAPSHOT.zip /shobdo-app/built.zip

# Use shell script to select the appropriate ZIP file
RUN if [ -f /shobdo-app/built.zip ] && [ "$BUILD_FROM_SOURCE" = "true" ]; then \
        cp /shobdo-app/built.zip /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT.zip; \
    elif [ -f /shobdo-app/prebuilt.zip ]; then \
        cp /shobdo-app/prebuilt.zip /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT.zip; \
    else \
        echo "Error: No application ZIP file found!"; \
        exit 1; \
    fi && \
    rm -f /shobdo-app/built.zip /shobdo-app/prebuilt.zip

# Unzip the application
RUN unzip /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT.zip -d /shobdo-app/bin/ && \
    chmod +x /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT/bin/shobdo-app

EXPOSE 9000

CMD /shobdo-app/bin/shobdo-app-0.11-SNAPSHOT/bin/shobdo-app -Dplay.crypto.secret=testtahsin

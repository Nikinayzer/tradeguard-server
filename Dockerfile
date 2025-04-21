# Build stage
ARG BUILD_PLATFORM
FROM --platform=${BUILD_PLATFORM} bellsoft/liberica-openjdk-debian:21 AS build
WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -X && ls -la target/

# Run stage
FROM --platform=${BUILD_PLATFORM} bellsoft/liberica-runtime-container:jre-21-slim-musl
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"] 
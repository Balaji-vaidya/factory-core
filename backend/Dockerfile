# ==============================================================================
# Factory Core Backend - Multi-stage Production Dockerfile
# Indo American Equipment / Agrisep®
# ==============================================================================

# Stage 1: Build JAR using Maven and Eclipse Temurin JDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source and package executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Production Runtime using Eclipse Temurin JRE 21
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run as dedicated unprivileged user for container security
RUN addgroup -S agrisep && adduser -S factorycore -G agrisep
USER factorycore:agrisep

# Copy packaged JAR from build stage
COPY --from=builder /build/target/factory-core-*.jar /app/factory-core.jar

# Environment defaults
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    JAVA_OPTS="-XX:+UseSerialGC -Xms128m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/factory-core.jar"]

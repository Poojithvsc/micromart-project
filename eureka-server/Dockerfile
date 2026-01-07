# ============================================================================
# Dockerfile for Eureka Server
# ============================================================================
# Multi-stage build for optimized image size
# ============================================================================

# Stage 1: Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY eureka-server/pom.xml eureka-server/

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -pl eureka-server -am

# Copy source code
COPY eureka-server/src eureka-server/src

# Build the application
RUN ./mvnw clean package -pl eureka-server -am -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S micromart && adduser -S micromart -G micromart
USER micromart

# Copy the built JAR from builder stage
COPY --from=builder /app/eureka-server/target/*.jar app.jar

# Expose Eureka Server port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

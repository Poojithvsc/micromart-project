# ============================================================================
# Dockerfile for Order Service
# ============================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY common/ common/
COPY order-service/pom.xml order-service/
RUN ./mvnw dependency:go-offline -pl order-service -am
COPY order-service/src order-service/src
RUN ./mvnw clean package -pl order-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S micromart && adduser -S micromart -G micromart
USER micromart
COPY --from=builder /app/order-service/target/*.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

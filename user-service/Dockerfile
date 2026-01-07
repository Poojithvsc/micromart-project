# ============================================================================
# Dockerfile for User Service
# ============================================================================
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY common/ common/
COPY user-service/pom.xml user-service/
RUN ./mvnw dependency:go-offline -pl user-service -am
COPY user-service/src user-service/src
RUN ./mvnw clean package -pl user-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S micromart && adduser -S micromart -G micromart
USER micromart
COPY --from=builder /app/user-service/target/*.jar app.jar
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

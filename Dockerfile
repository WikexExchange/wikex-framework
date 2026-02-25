# =============================================================================
# Wikex Platform - Multi-Service Dockerfile
# Usage: docker-compose up -d --build
# =============================================================================

# Step 1: Build ENTIRE PROJECT in one stage (shared across all services)
FROM maven:3.9.11-eclipse-temurin-8 AS full-builder
WORKDIR /build

# Copy entire project
COPY . .

# Build ENTIRE project once - all JARs will be available
RUN echo "🚀 Building entire Wikex project..." && \
    mvn clean package -DskipTests && \
    echo "✅ Build completed. Listing JAR files:"

# =============================================================================
# Step 2: Individual Service Stages (Extract JARs from full-builder)
# =============================================================================

# Runtime base image - Updated to use stable Eclipse Temurin
FROM eclipse-temurin:8-jre AS runtime-base
WORKDIR /app
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl tzdata && \
    rm -rf /var/lib/apt/lists/* && \
    cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    echo "Asia/Ho_Chi_Minh" > /etc/timezone && \
    mkdir -p /app/logs
COPY config/bootstrap.yml /app/config/bootstrap.yml

# Gateway Service (API Gateway)
FROM runtime-base AS gateway
COPY --from=full-builder /build/wikex-gateway/wikex-api-gateway/target/*.jar app.jar
ENV JAVA_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# User Service
FROM runtime-base AS user-service
COPY --from=full-builder /build/wikex-service/wikex-user-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx1g -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Market Service
FROM runtime-base AS market-service
COPY --from=full-builder /build/wikex-service/wikex-market-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Match Service (Multiple instances: match0, match1, match2)
FROM runtime-base AS match-service
COPY --from=full-builder /build/wikex-service/wikex-match-service/target/*.jar app.jar
ENV JAVA_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Exchange Service
FROM runtime-base AS exchange-service
COPY --from=full-builder /build/wikex-service/wikex-exchange-service/target/*.jar app.jar
ENV JAVA_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Swap Service
FROM runtime-base AS swap-service
COPY --from=full-builder /build/wikex-service/wikex-swap-service/target/*.jar app.jar
ENV JAVA_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Admin Service
FROM runtime-base AS admin-service
COPY --from=full-builder /build/wikex-service/wikex-admin-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx1g -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8087
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8087/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Agent Service
FROM runtime-base AS agent-service
COPY --from=full-builder /build/wikex-service/wikex-agent-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx512m -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8088
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8088/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Active Service
FROM runtime-base AS active-service
COPY --from=full-builder /build/wikex-service/wikex-active-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx1g -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8094
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8094/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Earn Service
FROM runtime-base AS earn-service
COPY --from=full-builder /build/wikex-service/wikex-earn-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx512m -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8095
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8095/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Chat Service
FROM runtime-base AS chat-service
COPY --from=full-builder /build/wikex-service/wikex-chat-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx512m -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8096
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8096/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Open Service
FROM runtime-base AS open-service
COPY --from=full-builder /build/wikex-service/wikex-open-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx512m -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8097
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8097/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Blog Service
FROM runtime-base AS blog-service
COPY --from=full-builder /build/wikex-service/wikex-blog-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx512m -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8098
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8098/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Kline Tools Service
FROM runtime-base AS kline-tools
COPY --from=full-builder /build/wikex-service/wikex-kline-tools/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx2g -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8098
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8098/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Coinswap Service
FROM runtime-base AS coinswap-service
COPY --from=full-builder /build/wikex-service/wikex-coinswap-service/target/*.jar app.jar
ENV JAVA_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8099
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8099/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Second Service
FROM runtime-base AS second-service
COPY --from=full-builder /build/wikex-service/wikex-second-service/target/*.jar app.jar
ENV JAVA_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8100
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8100/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Option Service
FROM runtime-base AS option-service
COPY --from=full-builder /build/wikex-service/wikex-option-service/target/*.jar app.jar
ENV JAVA_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
EXPOSE 8101
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8101/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# P2P Service
FROM runtime-base AS p2p-service
COPY --from=full-builder /build/wikex-service/wikex-p2p-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx1g -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8089
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8089/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Robot Market Service
FROM runtime-base AS robot-market-service
COPY --from=full-builder /build/wikex-service/wikex-robot-market-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx1g -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8090
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8090/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Robot Normal Service
FROM runtime-base AS robot-normal-service
COPY --from=full-builder /build/wikex-service/wikex-robot-normal-service/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx2g -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 8091
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8091/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]

# Job Admin Service
FROM runtime-base AS job-admin
COPY --from=full-builder /build/wikex-job/wikex-job-admin/target/*.jar app.jar
ENV JAVA_OPTS="-Xms512m -Xmx2g -Xmn200m -Xss256k -server -XX:+UseG1GC"
EXPOSE 58080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8086/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.config.additional-location=file:/app/config/ -jar app.jar"]


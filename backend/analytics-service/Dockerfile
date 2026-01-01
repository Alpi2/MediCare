# -----------------------------------------------------------------------------
# Stage 1: Build Stage
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 1. Önce sadece POM dosyalarını kopyala (Dependency Caching için)
# Bu katman değişmediği sürece Docker burayı cache'den çeker, hız kazanırsın.
COPY pom.xml .
COPY hospital-common/pom.xml hospital-common/pom.xml
COPY analytics-service/pom.xml analytics-service/pom.xml

# 2. Bağımlılıkları indir
RUN mvn dependency:go-offline -B

# 3. Kaynak kodlarını kopyala
COPY hospital-common/src hospital-common/src
COPY analytics-service/src analytics-service/src

# 4. Önce Common modülü "Install" et (Local repository'e kurar)
RUN mvn -f hospital-common/pom.xml install -DskipTests

# 5. Şimdi Analytics servisi paketle
RUN mvn -f analytics-service/pom.xml package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2: Runtime Stage
# -----------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Sağlık kontrolü için curl gerekli (Alpine imajında default gelmeyebilir)
RUN apk add --no-cache curl

# Build aşamasından çıkan JAR dosyasını al
# Dikkat: Jar yolu pom.xml ayarlarına göre değişebilir ama genelde target altındadır.
COPY --from=build /app/analytics-service/target/*.jar app.jar

RUN mkdir -p /app/logs

EXPOSE 8083

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
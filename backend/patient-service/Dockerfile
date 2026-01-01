FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom files for dependency caching (use backend as build context)
# We copy the patient-service and hospital-common poms and sources, then build the patient-service module.
COPY patient-service/pom.xml patient-service/pom.xml
COPY hospital-common/pom.xml hospital-common/pom.xml

# Download dependencies for the patient-service module
RUN mvn -f patient-service/pom.xml dependency:go-offline -B

# Copy source code for patient-service and hospital-common
COPY patient-service/src patient-service/src
COPY hospital-common/src hospital-common/src

# Build the patient-service module
RUN mvn -f patient-service/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install lightweight HTTP client for runtime healthchecks
RUN apk add --no-cache curl

# Copy JAR from build stage
COPY --from=build /app/patient-service/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Expose port
EXPOSE 8081

# Health check (use curl provided by apk)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]

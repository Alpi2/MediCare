# Patient Service

````markdown
# Patient Service

Patient Service - core microservice for patient management.

Tech: Spring Boot 3.2.0, Java 21, PostgreSQL, Redis, Kafka

Quickstart

1. Start infra (Postgres, Redis, Kafka)

```bash
docker-compose up -d postgres redis kafka
```

2. Setup database and topics

```bash
cd database/scripts
./setup-database.sh
cd ../../scripts
./setup-kafka-topics.sh
```

3. Build and run

```bash
cd backend/patient-service
mvn clean install
mvn spring-boot:run
```

API: http://localhost:8081/api/v1/patients
Swagger: http://localhost:8081/swagger-ui.html

Configuration: see `src/main/resources/application.properties`

## Environment Variables

### Required Variables

| Variable          | Description                    |         Example |
| ----------------- | ------------------------------ | --------------: |
| POSTGRES_PASSWORD | Database password              |      `changeme` |
| JWT_SECRET        | JWT signing key (min 32 chars) | `random-string` |

### Optional Variables (with defaults)

| Variable                |                                 Default | Description                |
| ----------------------- | --------------------------------------: | -------------------------- |
| POSTGRES_HOST           |                             `localhost` | Database host              |
| POSTGRES_PORT           |                                  `5432` | Database port              |
| POSTGRES_DB             |                           `hospital_db` | Database name              |
| POSTGRES_USER           |                         `hospital_user` | Database username          |
| REDIS_HOST              |                             `localhost` | Redis host                 |
| REDIS_PORT              |                                  `6379` | Redis port                 |
| KAFKA_BOOTSTRAP_SERVERS |                        `localhost:9093` | Kafka brokers              |
| JWT_EXPIRATION          |                                  `3600` | Token expiration (seconds) |
| JWT_ISSUER_URI          | `http://localhost:8080/realms/hospital` | OAuth2 issuer              |

### Setup

```bash
# Copy template
cp .env.example .env

# Edit with your values
nano .env

# Or use root .env.development
ln -s ../../.env.development .env

# Run service
mvn spring-boot:run
```

## Note about `.env` and Spring Boot

Spring Boot (when run via `mvn spring-boot:run` or as a packaged jar) does not automatically parse `.env` files placed in the project root. `.env` files are honored by Docker and many CI systems, but for local `mvn` runs you must either export the environment variables into your shell or add a tiny dotenv loader to your project.

Options to run locally:

- Export variables from `.env` into your shell (POSIX / zsh):

```bash
# load and export all variables from .env for the current shell
set -a && source .env && set +a
mvn spring-boot:run
```

- Or prefix the mvn command with required env vars (example):

```bash
SPRING_PROFILES_ACTIVE=docker POSTGRES_PASSWORD=changeme mvn spring-boot:run
```

- Alternatively, add a small dotenv loader to your application so `.env` files are read automatically (convenient for local development). Example (Maven dependency):

```xml
<!-- Example: add a dotenv/spring-dotenv library to load .env automatically -->
<dependency>
  <groupId>io.github.cdimascio</groupId>
  <artifactId>spring-dotenv</artifactId>
  <version>REPLACE_WITH_LATEST</version>
</dependency>
```

If you add a dotenv library, follow its documentation to enable automatic `.env` loading. Otherwise prefer exporting environment variables or using the Docker-based workflow which injects envs from `docker-compose.yml`.

### Docker

When running in Docker, use `application-docker.properties` profile:

```bash
SPRING_PROFILES_ACTIVE=docker mvn spring-boot:run
```

Environment variables are passed via `docker-compose.yml`.

### Validation

The service validates configuration on startup:

- Fails if `POSTGRES_PASSWORD` not set
- Warns if `JWT_SECRET` is too short
- Logs configuration with sensitive fields masked

See `docs/ENVIRONMENT_VARIABLES.md` for complete documentation.
````

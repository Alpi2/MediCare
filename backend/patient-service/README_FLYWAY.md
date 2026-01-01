Flyway configuration and usage

This service's Flyway Maven plugin is configured to use Maven properties so environments can override connection settings without changing the POM.

Defaults

- `flyway.url` (default: `jdbc:postgresql://localhost:5432/hospital_db`)
- `flyway.user` (default: `hospital_user`)
- `flyway.password` (default: `hospital_pass`)
- `flyway.schemas` (default: `public`)

Local overrides
You can run Flyway (via the Maven plugin) against a different database by passing system properties on the command line:

```bash
mvn -f backend/patient-service flyway:migrate -Dflyway.url=jdbc:postgresql://db:5432/hospital_db -Dflyway.user=my_user -Dflyway.password=secret
```

CI / scripts

- The repository contains `database/migrations/flyway.conf` which also declares `flyway.url`, `flyway.user`, and `flyway.password` for the migrations tool. CI pipelines and deploy scripts should set these properties (or pass -D overrides) instead of editing the POM.

Notes

- Keeping connection details outside the POM removes the need to edit source files per environment and reduces accidental commits of environment-specific values.
- For sensitive credentials, prefer injecting values from environment variables or CI secret stores rather than plaintext CLI args. Example with an environment variable in Bash:

```bash
mvn -f backend/patient-service flyway:migrate -Dflyway.url=${FLYWAY_URL} -Dflyway.user=${FLYWAY_USER} -Dflyway.password=${FLYWAY_PASSWORD}
```

If you want, I can also add a Maven profile that loads values from an external properties file for local development (e.g., `-Pflyway-local`).

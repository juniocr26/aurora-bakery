# Setup and execution

## Development with Docker

Install Docker with Compose v2. From a fresh clone:

```sh
if [ ! -e .env ]; then cp .env.example .env; fi
# Edit .env: use a local development password.
docker compose config --quiet
docker compose up --build -d --wait --wait-timeout 180
```

For an existing checkout, update `.env` from the documented variables rather than overwriting it. Application: http://localhost:4200/products. API: http://localhost:8080/api/v1/products. Local Swagger: http://localhost:8080/swagger-ui/index.html. Readiness: http://localhost:8080/actuator/health/readiness.

Flyway creates/updates the schema on backend startup; Hibernate validates it. `DEV_SEED_ENABLED=true` inserts three fictional products only in the `dev` profile, idempotently. No customer accounts or administrator credentials are seeded. Disable it for an empty catalog.

```sh
docker compose ps
docker compose logs --tail=100
docker compose stop
```



[Testing](testing.md).


[Host-persisted development dependencies and recovery](docker-development-setup.md). Base startup runs migrations and optional seed; use the existing development override to disable both when preserving an initialized database. Never run database-writing tests against application data.

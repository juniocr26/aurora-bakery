# Refactor verification — 2026-10-05

[English](verification.md) | [Português](../pt-BR/verification.md)

This records the earlier refactor verification, not a new full-stack run during the documentation review. The verification used Docker Desktop with Compose v2 on macOS ARM64. Host Java 17 and missing host Node were bypassed using the repository Java 25 and Node 22 Docker images. Exact pinned dependencies were retained.

| Check | Result |
| --- | --- |
| `docker compose config --quiet` | PASS |
| Fresh `docker compose build` | PASS, both application images |
| `docker compose up --build -d --wait --wait-timeout 180` | PASS, db/backend/frontend healthy |
| Fresh named volume and default network | Created as `aurora-bakery_db_data` and `aurora-bakery_default` |
| Flyway and Hibernate startup | PASS, V1/V2 applied and schema validated |
| Database query | PASS, both migration versions successful, two available and one temporarily unavailable demo products |
| Readiness with database | PASS, UP |
| Frontend `/products` | HTTP 200 |
| Angular proxy `/api/v1/products` | PASS, identical to direct backend response with three demo products |
| Angular production build | PASS, lazy catalog bundle |
| Angular tests | PASS, 4 tests |
| Backend unit and integration tests | PASS, 9 unit/API tests and 6 real PostgreSQL integration tests, zero failures or skips |
| Container startup/runtime logs | No application startup errors; normal PostgreSQL initialization restart and development server/toolchain warnings |

No full browser rendering, authenticated checkout, payment, ADMIN provisioning or production deployment verification is claimed. Those workflows do not exist yet. No admin bootstrap is required for the current public read-only catalog. Required services have no previous project name; immutable V1 migration retains historical directory terminology only.

## Commands executed

```sh
docker compose config --quiet
docker compose build
docker compose up --build -d --wait --wait-timeout 180
docker compose ps
docker compose logs --tail=60
docker build --target build -t aurora-bakery-backend-tests ./backend
docker compose run --rm --no-deps frontend npm test
docker compose run --rm --no-deps frontend npm run build
```

Backend tests ran with the Docker socket and `TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal`, executing `./mvnw -B -ntp verify -Pintegration` in the build-stage image. A read-only source mount included the final test changes during verification. The README documents a rebuilt test image command for fresh clones. Database and HTTP smoke checks used `docker compose exec -T db` (psql) and `docker compose exec -T frontend` (Node fetch), checking migrated rows, API/proxy equality and readiness.

## Architecture documentation review — 2026-10-05

This separate review ran the current backend unit/API source in the existing Java 25 build-stage test image, with a read-only source mount: `docker run --rm -v "$PWD/backend/src:/workspace/src:ro" aurora-bakery-backend-tests ./mvnw -B -ntp verify`. All 9 unit/API tests passed. The initial network-disabled attempt failed because the Maven Wrapper distribution was not available in the image; rerunning with downloads enabled passed. No integration profile, frontend suite, browser checks or application Compose stack was run in this review. The disposable container was removed, and no application database/volume or real environment file was mounted. Earlier full-stack results above are historical.

## Documentation validation — 2026-10-06

No containers were running before this project. `docker compose -f docker-compose.yml -f compose.development.yaml config --quiet` passed. The same files with `up -d` started db/backend/frontend, with Flyway and seed disabled and Hibernate set to validate. Readiness, direct product API, frontend `/products` and proxied API returned HTTP 200 after readiness (an early probe failed while backend was starting). `exec -T frontend npm test` passed 4 tests. Backend `exec -T backend sh ./mvnw -B -ntp test` initially failed 1 of 9 because it inherited `APP_ENV=dev`; `exec -T -e APP_ENV=default backend sh ./mvnw -B -ntp test` passed all 9. No application code was changed. Integration tests and production build were not rerun. No database-writing checks were executed. `stop frontend backend db` stopped all services started here, preserving containers and volumes. Language pairs and local Markdown links/anchors were checked.

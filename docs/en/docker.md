# Development Docker environment

[English](docker.md) | [Português](../pt-BR/docker.md)

Compose project: `aurora-bakery`. Services: `db`, `backend`, `frontend`. Compose generates `aurora-bakery-db-1`, `aurora-bakery-backend-1`, `aurora-bakery-frontend-1`, network `aurora-bakery_default`, volume `aurora-bakery_db_data` and application images `aurora-bakery-backend` / `aurora-bakery-frontend`. Explicit container names are unnecessary. PostgreSQL retains its official pinned image.

The prior `postgres` service is renamed to `db`; no whole service was removed because all three remain required. Obsolete `commerce`, `web`, `db_access` networks, unused `postgres_data` volume declaration and configurable legacy database bind mount are removed. One default bridge network is sufficient. Host listeners bind only to loopback. PostgreSQL is reachable internally at `db:5432`, backend at `backend:8080`, and Angular at `frontend:4200`.

Backend waits for database health, frontend waits for backend readiness. Backend readiness includes database connectivity. Angular's proxy uses `API_PROXY_TARGET=http://backend:8080`; browsers call relative `/api` URLs. Health checks remain on all services. Backend runs as a non-root `aurora` user. Frontend runs as `node`. Docker ignores exclude secrets, generated files and host dependencies. The frontend Dockerfile is a development/build image, with no production hosting claim.

## Configuration

| Variable | Purpose / default |
| --- | --- |
| APP_ENV | `dev` in Compose; dev enables local OpenAPI and permits demo seed |
| POSTGRES_DB / POSTGRES_USER | `aurora_bakery` |
| POSTGRES_PASSWORD | Required local value; never use example outside development |
| POSTGRES_HOST_PORT | Loopback database listener, 5432 |
| BACKEND_HOST_PORT | Loopback API listener, 8080 |
| FRONTEND_HOST_PORT | Loopback Angular listener, 4200 |
| ALLOWED_ORIGINS | Explicit comma-separated browser origins; update if frontend port changes |
| DEV_SEED_ENABLED | Opt-in demo products; false in Compose, true in example |
| ADMIN_DISCOUNT_PERCENT | Backend purchase policy, 15 by default, range 0–100 |
| APP_UID / APP_GID | Non-root backend build user, 10001 |
| DB_URL | Host backend override; Compose supplies JDBC URL using `db` |
| API_PROXY_TARGET | Host frontend override; Compose supplies backend hostname |

`POSTGRES_DATA_SOURCE` and previous configurable Maven cache identity are no longer used. `.env` stays ignored. There are no Stripe or administrator secrets in committed configuration. For SQL clients: localhost, configured database port, database/user/password from `.env`.

See the root README for startup, shutdown, reset and Docker-only testing commands. Backend edits require `docker compose up --build -d --wait`; frontend source is mounted read-only for live development. Database migrations and optional demo bootstrap run automatically at backend startup. Production overrides, reverse proxies, Makefiles, Docker scripts and tracked CI pipelines were absent and are not introduced.

For host-persisted development dependencies and safe recovery, follow [Docker development setup](docker-development-setup.md). Its explicit override differs from the base image-based setup above.

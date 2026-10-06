# Aurora Bakery

[English](README.md) | [Português](README.pt-BR.md)

Aurora Bakery is a single-store bakery e-commerce portfolio project built with Java and Angular. Its target scope covers catalog, ordering, promotions, loyalty, authentication, authorization, Stripe test payments and e-commerce administration, while avoiding unnecessary multi-tenant and microservice complexity.

**Implemented today:** read-only product catalog, explicit availability, featured ordering, BRL prices, Angular loading/empty/error/retry states, Flyway migrations, optional fictional development products, restrictive backend security, health probes and local OpenAPI. A tested backend purchase policy defines configurable ADMIN discounts and exclusion from customer benefits and marketing; it is not yet connected to checkout.

**Planned:** shared persisted CUSTOMER/ADMIN identity, customer registration/login, controlled administrator provisioning, administrative writes and image uploads, cart, orders, promotion configuration, paid-order loyalty progression, Stripe test checkout/webhooks and notifications. There is currently no purchasing or login workflow.

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
docker compose down
```

The named database volume persists across `down`. To intentionally reset local development data: `docker compose down --volumes`, then run the startup command again. This destroys that Compose project's database. Legacy `.dockerized-postgres` data is no longer mounted.

## Tests

No host Java or Node is required for the Docker workflow:

```sh
docker build --target build -t aurora-bakery-backend-tests ./backend
docker run --rm aurora-bakery-backend-tests ./mvnw -B -ntp verify
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal aurora-bakery-backend-tests ./mvnw -B -ntp verify -Pintegration
docker compose run --rm --no-deps frontend npm run build
docker compose run --rm --no-deps frontend npm test
```

The integration runner needs access to a local Docker daemon and uses a separate disposable PostgreSQL container; it does not modify the application database. With JDK 25 and Node 22.23.3 on the host, use `./mvnw verify -Pintegration` in `backend` and `npm ci && npm run build && npm test` in `frontend`.

## Architecture and documentation

One Spring Boot 3.5.16 application (Java 25, Maven Wrapper), one Angular 21 application (npm lockfile), PostgreSQL 17.11. Existing dependency versions and immutable base image digests are retained. No message broker, identity provider, reverse proxy, additional application or CI pipeline existed in the source baseline.

- [Architecture and domain](docs/en/architecture.md)
- [Architecture Decisions & Trade-offs](docs/architecture-decisions.md)
- [Docker and environment variables](docs/en/docker.md)
- [Scope and next steps](docs/en/roadmap.md)
- [Verification](docs/en/verification.md)
- [Refactor deliverable and limitations](docs/refactor-report.md)

The next implementation step is persisted identity with CUSTOMER-only registration, password hashing, internal ADMIN provisioning and backend authorization before enabling administrative mutations. SSO, WhatsApp, automatic product archiving, object storage and production deployment are deferred. Payroll, ERP, accounting, franchises and tenant isolation are excluded.

## License

Júnio Rosa · [MIT](LICENSE)

For safe host-persisted dependencies and configuration recovery, see [Docker development setup](docs/en/docker-development-setup.md).

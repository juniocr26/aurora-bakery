# Architecture Decisions & Trade-offs

[English](architecture-decisions.md) | [Português](../pt-BR/architecture-decisions.md)

This document explains the current single-store catalog and its intended e-commerce evolution. Implementation statements come from source, migrations, configuration and tests. Unless an existing document records intent, the rationale below evaluates the current architecture rather than claiming to recover its original motivation. Alternatives are review options, not evidence of a historical evaluation.

## Implementation status

- **Implemented:** Spring Boot catalog, PostgreSQL schema, Angular storefront, deny-by-default HTTP security, Docker development environment and catalog/pricing tests.
- **Designed / architecturally prepared:** CUSTOMER/ADMIN role vocabulary and an executable purchase policy. Neither constitutes persisted identity, loyalty tracking or checkout.
- **Planned / future work:** login, controlled administrator provisioning, administrative writes, orders, uploads, promotions, loyalty progression, Stripe test payments and notifications. See the [roadmap](roadmap.md).

## Decision: One bakery in a modular monolith

**Context.** The current product explores a bakery storefront, not merchant onboarding or franchise management.

**Decision and why.** One Spring Boot application contains catalog, identity vocabulary and pricing packages. The products schema has no tenant ownership. This keeps catalog queries and future order transactions within one application/database boundary; microservices would add network failures and deployment coordination before there are independent workflows to deploy.

**Alternatives.** A multi-tenant SaaS would require tenant-aware keys, authorization and isolation tests. Independently deployed catalog/order services would require contracts and consistency across services.

**Trade-offs and consequences.** Deployment and debugging are simpler, but package boundaries are conventions rather than isolated services. Catalog separates controller, application service, domain entity and JPA repository; the service directly imports the infrastructure repository, so this is not strict dependency inversion. Payroll, ERP and accounting remain out of scope. No broker or background processing is implemented.

**Revisit when.** Independently owned modules need different deployment schedules or measured workloads require independent scaling. A multi-store product would require an explicit scope change and data-isolation design.

**Evidence:** [CatalogService](../../backend/src/main/java/com/aurorabakery/catalog/application/CatalogService.java), [V2 schema](../../backend/src/main/resources/db/migration/V2__single_bakery_catalog.sql), [domain overview](architecture.md).

## Decision: Java and Spring Boot for typed application boundaries

**Context.** Catalog data and future monetary rules need explicit contracts and testable server-side behavior.

**Decision and why.** Java records express response DTOs, enums constrain availability/roles, and `BigDecimal` makes rounding explicit. Spring MVC, dependency injection, Security, JPA, Flyway and Actuator integrate HTTP, persistence and operations in one runtime. The present benefit is visible in a thin controller and transactional read service; it is not a claim of measured performance or an original language-selection motive.

**Alternatives.** A smaller Python/FastAPI or Go HTTP application could serve this catalog but would require different persistence/security integration. Spring JDBC could make SQL more explicit than JPA.

**Trade-offs and consequences.** Framework configuration and a Java 25 toolchain are required even for a small read API. ORM annotations couple the product entity to persistence, and Spring annotations appear in the pricing policy; logical boundaries are clear but not framework independent. Explicit DTO mapping avoids exposing entity internals and allows the public contract to evolve separately.

**Revisit when.** Framework cost materially impedes deployment, or query complexity warrants explicit SQL. First measure the actual workload.

**Evidence:** [pom.xml](../../backend/pom.xml), [ProductSummary](../../backend/src/main/java/com/aurorabakery/catalog/application/ProductSummary.java), [PurchasePolicy](../../backend/src/main/java/com/aurorabakery/pricing/domain/PurchasePolicy.java).

## Decision: PostgreSQL with versioned schema ownership

**Context.** The implemented catalog needs unique slugs, valid lifecycle states and positive decimal prices. Future orders/customers are relational, but those tables do not exist today.

**Decision and why.** PostgreSQL enforces checks and uniqueness; Flyway owns schema evolution and Hibernate validates rather than generates tables. `open-in-view=false` and mapping inside a read-only transaction keep persistence work out of HTTP serialization.

**Alternatives.** An embedded database would reduce local infrastructure but would not exercise PostgreSQL-specific constraints. Document storage would move more consistency checks into application code. Automatic ORM schema updates would obscure migration history.

**Trade-offs and consequences.** Database changes need explicit migrations. V1 remains immutable; V2 renames `stores` to unexposed `legacy_stores` instead of deleting old rows. Fresh databases also retain that unused table. The catalog loads all visible products into a list; there is no pagination or cache, so response size and database work grow with the catalog.

**Revisit when.** Catalog size warrants pagination/index analysis, or a reviewed retention policy permits removing legacy data. Orders will need transaction and snapshot rules before implementation.

**Evidence:** [migrations](../../backend/src/main/resources/db/migration), [application.yml](../../backend/src/main/resources/application.yml), [PostgreSQL integration tests](../../backend/src/test/java/com/aurorabakery/catalog/ProductPostgresIT.java).

## Decision: Availability and historical lifecycle instead of inventory

**Context.** Existing domain documentation models made-to-order bakery products rather than ingredient or warehouse accounting.

**Decision and why.** AVAILABLE and TEMPORARILY_UNAVAILABLE remain public; only AVAILABLE is marked purchasable. DISCONTINUED and ARCHIVED are excluded from the public query. Availability answers whether a product can be offered without pretending to measure physical stock. Featured ordering is independent of discounts.

**Alternatives.** Quantity reservations would support finite stock but require concurrency, release/expiry and overselling rules. Physical deletion would simplify storage but lose records useful to future historical references.

**Trade-offs and consequences.** Temporary unavailability remains understandable to customers rather than appearing as a missing product. Rows are preserved, but no mutation API, automatic archive scheduler or order references exist yet. `purchasable` is catalog information, not checkout enforcement. The UI does not offer purchases.

**Revisit when.** Finite batches require stock reservations, or administrative lifecycle/retention requirements become concrete.

**Evidence:** [Product](../../backend/src/main/java/com/aurorabakery/catalog/domain/Product.java), [CatalogService](../../backend/src/main/java/com/aurorabakery/catalog/application/CatalogService.java), [ProductPostgresIT](../../backend/src/test/java/com/aurorabakery/catalog/ProductPostgresIT.java).

## Decision: One Angular storefront with explicit request states

**Context.** A customer must distinguish a slow request, an empty catalog and a backend failure.

**Decision and why.** One Angular app lazily loads the catalog; `ProductApi` owns HTTP access and a discriminated signal state drives loading/error/ready rendering. BRL formatting, status/alert roles, a skip link and a retry button turn API state into useful feedback. Relative `/api` calls use the development proxy, avoiding Docker hostnames in browser code.

**Alternatives.** Server-rendered HTML would reduce client tooling for a read-only catalog. A separate admin frontend would permit independent releases but duplicate tooling and shared contracts.

**Trade-offs and consequences.** Angular supplies typed components and testing support at the cost of a build/runtime toolchain. Retry issues a fresh synchronous HTTP request; no offline cache or automatic retry exists. The API returns image metadata, but the current frontend does not render product images. Shared storefront/admin tooling is a planned direction: there are no admin routes or guards today.

**Revisit when.** Search indexing/server rendering becomes a requirement, or administration needs an independent release/security boundary.

**Evidence:** [catalog UI and tests](../../frontend/src/app/catalog), [bootstrap/routes](../../frontend/src/main.ts), [proxy](../../frontend/proxy.conf.cjs).

## Decision: Deny writes until identity is implemented

**Context.** A role enum cannot authenticate users or authorize administrative operations.

**Decision and why.** Security allows public catalog GETs, non-sensitive health and dev-only OpenAPI, then denies all remaining routes. Explicit CORS origins and generic Problem Details limit accidental exposure. This provides a safe read-only boundary while identity is unfinished.

**Alternatives.** Temporary public writes would expose privileged operations. An identity provider could provide federation but would add infrastructure before the current login workflow exists.

**Trade-offs and consequences.** No customer login or actual administrator access is possible. Stateless configuration and disabled CSRF describe the current read API, not a settled future authentication strategy. Cookie-based authentication would require revisiting CSRF; bearer authentication would require token validation and lifecycle rules. Exception details are hidden from clients but logged on the server.

**Planned decision.** One persisted identity will use CUSTOMER/ADMIN roles; public registration must create CUSTOMER only. Internal ADMIN provisioning must hash passwords and avoid migration/seed credentials. Backend authorization remains authoritative even if frontend guards are added.

**Revisit when.** Any authenticated or mutation endpoint is introduced; select session/token handling before extending the allowlist.

**Evidence:** [SecurityConfiguration](../../backend/src/main/java/com/aurorabakery/configuration/SecurityConfiguration.java), [API/security tests](../../backend/src/test/java/com/aurorabakery/catalog/ProductApiTest.java), [identity roadmap](roadmap.md).

## Decision: Centralize monetary policy before checkout

**Context.** Discount stacking and administrator eligibility need one deterministic server policy.

**Decision and why.** `PurchasePolicy` applies a configurable ADMIN percentage (default 15%), excludes ADMIN from loyalty/marketing, and chooses the greater eligible promotion/loyalty percentage for CUSTOMER. It validates percentage ranges and rounds totals to two decimal places with HALF_UP. Keeping these rules outside controllers makes policy tests independent of HTTP/database setup.

**Alternatives.** UI calculations duplicate business rules and can be tampered with. Stacked discounts change commercial behavior and complicate explanation of totals.

**Trade-offs and consequences.** Rules are testable now, but caller-supplied roles/eligible percentages are only method inputs, not authenticated facts. No promotion persistence, paid-order counting, marketing delivery or purchase endpoint exists. Future checkout must derive eligibility and totals server-side and snapshot the applied values into orders. Promotional consent must remain separate from transactional communication.

**Revisit when.** Promotion precedence, refunds, loyalty reversals or per-item rounding become requirements.

**Evidence:** [policy](../../backend/src/main/java/com/aurorabakery/pricing/domain/PurchasePolicy.java), [policy tests](../../backend/src/test/java/com/aurorabakery/pricing/PurchasePolicyTest.java).

## Decision: Keep storage and payment integrations planned until their workflows exist

**Context.** The catalog currently stores optional image-path metadata and has no checkout.

**Planned decision and why.** Existing architecture proposes a local filesystem storage adapter behind a port and a Stripe test adapter behind a payment port. Local storage would avoid cloud setup for one instance; Stripe test mode would demonstrate provider-backed payment without real-money processing. Neither port/adapter is implemented today.

**Alternatives.** Object storage supports shared files across instances but adds credentials and operations. Database blobs couple file volume to database backup. A fake frontend payment success is simpler but cannot establish provider-confirmed payment state.

**Trade-offs and consequences.** Before uploads, define size/type validation, generated names and traversal protection. Before Stripe, implement server-owned order totals, signature verification and idempotent webhooks. A redirect to a success screen must not establish PAID state. Durable asynchronous delivery/outbox is a future reliability decision, not a current component.

**Revisit when.** Uploads or checkout are implemented; shared deployments require shared/object storage, and payment retries require explicit idempotency and recovery rules.

**Evidence:** [planned boundaries](architecture.md), [roadmap](roadmap.md), [dependency list](../../backend/pom.xml).

## Decision: Docker for local reproducibility and layered verification

**Context.** Java, Node and PostgreSQL versions must be reproducible without relying on host toolchains.

**Decision and why.** Compose separates db/backend/frontend on one bridge network, binds host ports to loopback, persists PostgreSQL in a named volume and gates startup on health. Pinned image digests and the npm lockfile reduce dependency drift. The backend runtime and frontend use non-root users. The frontend is a development server, not production hosting.

**Alternatives.** Host-only setup removes containers but requires matching toolchains; production orchestration/reverse proxies introduce concerns this local environment does not solve.

**Trade-offs and consequences.** Database startup automatically runs migrations; optional dev seed inserts fictional products, so launching against existing data is a write operation. Seed SQL is separate from migrations and idempotent. Health/readiness includes database connectivity but is not full production observability. Persistent volumes survive ordinary shutdown.

**Testing choice.** MockMvc with a mocked service checks HTTP/security contracts; direct policy tests check monetary rules; opt-in Testcontainers PostgreSQL tests exercise actual migrations, constraints, query behavior and repeatable seed. Angular HTTP fakes check request/UI states. Fakes do not prove database behavior; real database tests cost Docker startup and resources. No checkout/payment tests are claimed.

**Revisit when.** Production hosting, deployment automation or failure recovery is required. Keep test database configuration isolated from development data.

**Evidence:** [Compose](../../docker-compose.yml), [Docker details](docker.md), [tests](../../backend/src/test/java/com/aurorabakery), [frontend tests](../../frontend/src/app/catalog/product-list.spec.ts).

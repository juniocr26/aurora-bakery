# Architecture and domain

> Historical commerce scope. Payment Reconciliation Lab retires the purchase policy and catalog seed. Reconciliation and Stripe remain planned; current infrastructure and transition instructions are in [Docker](docker.md).


[English](architecture.md) | [Português](../pt-BR/architecture.md)

Aurora Bakery represents one bakery. A Spring Boot modular monolith exposes explicit DTOs to one Angular application through `/api/v1/products`. Angular's development proxy forwards `/api/**` to the backend; browser URLs remain relative. PostgreSQL stores application data. No store selection or store-scoped authorization remains.

## Implemented modules

Catalog separates API, application, domain and persistence. Products contain slug, name, description, BRL price, category, optional relative image reference, featured status, availability and timestamps. Featured status is independent of discounts. The public query includes AVAILABLE and TEMPORARILY_UNAVAILABLE products, ordered by featured status and name. Only AVAILABLE is purchasable. DISCONTINUED and ARCHIVED are hidden, with records preserved. There is no delete API or inventory quantity.

Identity currently defines the CUSTOMER and ADMIN vocabulary only; no persisted account model or login exists. Pricing contains a pure backend purchase policy: configurable ADMIN discount (default 15%), exclusion from loyalty and marketing regardless of consent, and no stacking with customer benefits. Customers receive the greater of eligible promotion/loyalty percentages. This policy is tested but not connected to a purchase endpoint. Future checkout must resolve identity and eligible benefits server-side; never accept role or discount eligibility from clients.

Security permits public catalog reads, non-sensitive health and dev-only OpenAPI; unmatched and mutation routes are denied. There are no publicly assignable roles or password fields. Existing stateless/CSRF-disabled configuration is appropriate only to the current read-only API; revisit it when selecting session or bearer authentication. Explicit CORS origins are required. API failures do not expose exception details.

## Database evolution

V1 remains unchanged to preserve Flyway checksums. V2 renames the former directory table to `legacy_stores` for historical preservation and creates the single-bakery products table. Legacy rows are not exposed or used by application code. A fresh database also applies both migrations. Development fixture SQL is separate from schema migrations. Hibernate uses validation, not schema generation.

## Planned lifecycle and boundaries

Persist one account identity with CUSTOMER/ADMIN roles. Public registration creates CUSTOMER only and validates name, email, password and CPF; phone and marketing consent are optional. Provision ADMIN through an explicit internal command that hashes an environment-supplied password and never changes existing roles silently. No public admin registration. ADMIN can shop with the purchase policy and receives transactional messages for their own orders.

Future orders snapshot product name, unit price and applied discounts. The proposed minimal lifecycle is AWAITING_PAYMENT → PAID → PREPARING → READY → COMPLETED, with CANCELLED/PAYMENT_FAILED branches and validated transitions. Loyalty must count verified paid/completed orders once, never cancelled/failed payments. Refund/reversal behavior needs definition before implementation.

Stripe test payments belong behind a payment port; verified signed webhooks, idempotent handling and server-owned totals establish payment state. No Stripe dependency, keys, endpoint or fake payment currently exists. Marketing requires customer consent; WhatsApp also requires optional phone and explicit opt-in. Transactional communication has separate preferences/purpose. Notifications can start with application-level processing; reliability requirements should determine any durable outbox later.

Future product uploads use a storage port with local filesystem implementation, relative metadata, generated filenames, size/type checks and traversal protection. No upload endpoint exists yet. Object storage is preferable for horizontal production scaling. Archive scheduling can use Spring scheduling and configurable age without extra services.

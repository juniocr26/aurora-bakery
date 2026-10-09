# Scope and implementation status

> Historical commerce scope. Payment Reconciliation Lab retires the purchase policy and catalog seed. Reconciliation and Stripe remain planned; current infrastructure and transition instructions are in [Docker](docker.md).


[English](roadmap.md) | [Português](../pt-BR/roadmap.md)

## Completed in this restructuring

Single-bakery identity, catalog migration/read API, Angular catalog, availability and featured visibility, independent backend pricing/consent policy, meaningful catalog/security/pricing tests, simplified development Docker and current English documentation.

## Required future product increments

1. Shared persisted identity, secure CUSTOMER registration/authentication, optional phone and communication preferences, controlled internal ADMIN provisioning and backend role authorization.
2. Administrative catalog mutations, safe local image storage port and product lifecycle transitions; lazy administrative routes in the same Angular app.
3. Persisted date-based product promotion rules and configurable loyalty rules, administrative management and service tests.
4. Cart/checkout, server-priced historical order snapshots and validated state transitions.
5. Stripe test adapter, signature-verified idempotent webhooks, paid-order loyalty progress and transactional notifications.
6. Customer order history, loyalty UI, marketing opt-in enforcement and authorized order operations.

These were absent in the original repository, which contained only a read-only directory. They require new workflows, not simply removal of tenant code. They are not claimed as implemented. ADMIN discount tests exercise the policy only; there are no real admin purchases yet. No authentication, paid-order loyalty, Stripe or admin endpoint tests are claimed. This refactor establishes the reusable foundation requested by the incremental implementation strategy.

## Intentionally deferred

Automatic discontinued-to-archived scheduling (configurable age), object storage, WhatsApp, SSO, production frontend hosting and reliable background delivery. No extra services are justified today. Payroll, HR, ERP, accounting, tenant isolation, merchant onboarding and franchises are permanently outside the current scope.

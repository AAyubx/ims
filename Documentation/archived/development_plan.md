# Development Plan — Modern Store Inventory Management System (Java, Microservices, MySQL)
_Last updated: 2025-08-31 12:28 UTC_

## Target Stack
- **Language/Framework:** Java 17+, Spring Boot 3.x, Spring Data/JPA, MapStruct
- **Architecture:** Microservices; Domain-driven modules (Catalog, Inventory, Purchasing, Pricing, Orders, Reporting, Auth, Notifications, Mobile Edge)
- **Messaging/Eventing:** Kafka or RabbitMQ (domain events)
- **API:** REST (OpenAPI), GraphQL (optional for read paths)
- **DB:** MySQL 8 (InnoDB) primary + replicas (read scale), per-service schema
- **Build/CI:** Maven/Gradle, Git-based CI pipelines, SonarQube
- **Containerization:** Docker; dev via Tilt/Dev Containers
- **Observability (dev):** Testcontainers + local Prometheus/Grafana; OpenTelemetry SDK

## Domain → Service Map
| Domain | Service | Primary Store | Notes |
|---|---|---|---|
| Catalog | catalog-svc | MySQL schema `catalog` | Items, variants, attributes, categories |
| Inventory | inventory-svc | MySQL schema `inventory` | InventoryLedger (event-sourced), StockSummary, reservations |
| Purchasing | purchasing-svc | MySQL schema `purchasing` | Suppliers, POs, receipts |
| Pricing/Promo | pricing-svc | MySQL schema `pricing` | Price lists, promotions |
| Orders/Fulfillment | order-svc | MySQL schema `orders` | Reservations, pick/pack/ship intents |
| Reporting | reporting-svc | MySQL replica/OLAP sink | Materialized views/ETL to lake |
| Auth/RBAC | auth-svc (Keycloak external) | N/A | OIDC roles/claims |
| Notifications | notify-svc | MySQL `ops` | Email/SMS/webhooks |
| Mobile Edge | edge-svc | SQLite/embedded cache | Offline queue + sync |

---

## Database Architecture (Cross-cutting)
- **MySQL 8 (InnoDB), row-based replication.** Per-service schema to enforce bounded contexts and reduce cross-service coupling.
- **Patterns:**
  - **CQRS-lite:** write models in core services; read models/materialized views for dashboards.
  - **Event sourcing for inventory movements:** immutable `inventory_ledger` forms source of truth; `stock_summary` is derived.
  - **Sharding/partitioning:** range/hash partition by `(location_id)` or `(location_id, sku_id)` for `inventory_ledger` and `stock_summary` when volume grows.
  - **Read scaling:** replicas for reporting/queries; ProxySQL or application routing.
  - **Transactions:** use **outbox pattern** for publishing domain events (Debezium CDC optional).
  - **Optimistic concurrency** on stock reservations; pessimistic row locks for high‑contention adjustments.
  - **Indexes:** composite covering indexes (e.g., `ledger(location_id, sku_id, ts)`), full‑text for catalog search mirrored to OpenSearch if required.
  - **Archival:** move closed ledger periods to cheaper storage via ETL.
- **Baseline Entities (simplified DDL):**
```sql
-- catalog
CREATE TABLE item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sku VARCHAR(64) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  brand VARCHAR(128),
  category_id BIGINT,
  status ENUM('DRAFT','ACTIVE','DISCONTINUED') NOT NULL,
  created_at TIMESTAMP, updated_at TIMESTAMP
);

-- inventory
CREATE TABLE inventory_ledger (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  location_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  ref_type VARCHAR(32) NOT NULL, -- RECEIPT, SHIPMENT, TRANSFER, ADJUSTMENT, COUNT
  ref_id VARCHAR(64) NOT NULL,
  qty INT NOT NULL,
  reason_code VARCHAR(32),
  ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  user_id BIGINT,
  INDEX ix_led_loc_sku_ts (location_id, sku_id, ts)
);

CREATE TABLE stock_summary (
  location_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  on_hand INT NOT NULL,
  reserved INT NOT NULL,
  available INT NOT NULL,
  PRIMARY KEY (location_id, sku_id)
);
```

---

## Development Practices (applies to all features)
- TDD/BDD for core domain logic; contract tests (Spring Cloud Contract/Pact) for service boundaries.
- OpenAPI-first; generate clients and server stubs; lint schemas in CI.
- Migrations via **Flyway** per service (`db/migration`).
- Feature flags (Unleash/FF4J) for progressive delivery.
- Testcontainers for MySQL/Kafka in integration tests.
- Static analysis (SpotBugs, Checkstyle), SAST/Dependency scans.
- Seed data & golden scenarios for end‑to‑end tests.

---

## Feature-by-Feature Development Plan

### 1) Architecture & Platform
- Bootstrap Spring Boot services; standard starters (web, validation, data-jpa, security).
- Establish common libraries: logging, tracing, error model, response envelope, OpenAPI config.
- Implement API Gateway (Spring Cloud Gateway) and OAuth2 resource server config.
- Define domain events contract (`InventoryAdjusted`, `PurchaseOrderReceived`, etc.).
- Set up Monorepo or Polyrepo with shared BOM and versioning.

### 2) Multi‑Store, Multi‑Channel, Multi‑Tenant
- Add `organization_id` (if multi-tenant), `location` domains; enforce tenant scoping via JWT claims.
- Channel adapters: POS, ecommerce, ERP connectors behind ports/adapters.
- Validation rules per store (safety stock, lead times) as policy objects.

### 3) Item & Catalog Management
- CRUD for items/variants/categories; attribute model (EAV or JSON columns).
- Search projection (optional) to OpenSearch; domain events on item lifecycle.
- Versioning: optimistic locking; shadow drafts for safe edits.

### 4) Stock Control & Movements
- Implement ledger write model with command handlers (receive, ship, transfer, adjust, count).
- Compute `stock_summary` via transactional upsert or async projector.
- Serial/lot tables when enabled; FEFO queries for expiries.
- Idempotency keys on inbound operations (ref_id + ref_type).

### 5) Purchasing & Replenishment
- Supplier, PO, Receipt aggregates; state machine (`DRAFT->APPROVED->RECEIVED`).
- Forecasting interface with pluggable engine; Min/Max policy to start.
- Approval workflow (Camunda optional) for replenishment proposals.

### 6) Pricing & Promotions
- Price list model with effective dates; promo engine with rule set abstraction.
- Simulation API; audit trail for changes.

### 7) Barcoding, Scanning & RFID
- Label template entities; ZPL/PDF generator microservice.
- Mobile app endpoints for scan flows (receive/transfer/count).
- RFID adapter service: ingest read events → reconcile to ledger.

### 8) Orders, Reservations & Fulfillment
- Reservation service: atomic check & deduct using optimistic concurrency on `stock_summary`.
- Pick/pack flows; picklist generation by zone/bin; validation API.

### 9) Data Quality & Governance
- Validation service for reference data; stewardship UI for resolving duplicates.
- Change data capture to audit table; entity history endpoints.

### 10) Security & Compliance
- RBAC roles (Admin, Manager, Clerk, Viewer); method-level security via annotations.
- Field-level masks for cost visibility; encrypt sensitive columns (MySQL TDE/KMS).

### 11) Observability & Reliability
- Add Micrometer metrics, traces, and structured logs.
- Implement circuit breakers/retries with Resilience4j; consistent error codes.

### 12) Reporting, Analytics & Insights
- Build materialized views; ETL to lakehouse (Parquet) using Debezium/Batch jobs.
- KPI endpoints (stockouts, turns, aging).

### 13) Extensibility & Integration
- Webhooks publisher; subscription management.
- SFTP/CSV gateway with schema validation and quarantine folder.

### 14) User Experience (Web & Mobile)
- Admin UI (React/Vaadin) scaffolded; authentication via OIDC implicit/hybrid.
- Mobile scanning flows with offline queue (workbox/SQLite), sync API endpoints.

### 15) Performance & Scalability Targets
- Load tests with Gatling/JMeter; set p95 SLOs per endpoint.
- Profiling (JFR), connection pool sizing, batching strategies.

### 16) Testing & Quality
- Unit + slice tests; integration tests with Testcontainers.
- Consumer-driven contracts for cross-service APIs.
- E2E pipelines using Playwright/Cypress for UI flows.

### 17) DevSecOps & Delivery
- Maven release plugin, SemVer, changelog.
- Pre-commit hooks; SBOM via CycloneDX; SCA with OWASP Dependency-Check.

### 18) Data Model (Minimum Entities)
- Normalize core entities; use GUIDs for external references, BIGINT for internal keys.
- Soft delete with `deleted_at` where required; triggers to maintain history where appropriate.

### 19) Accessibility & Internationalization
- i18n message bundles; locale-aware formatting and currency handling.
- Keyboard-accessible UI components; automated a11y checks (axe).

### 20) Governance Checklists (Go‑Live Readiness)
- Define API versioning policy; deprecation headers.
- Data migration runbook; dual-write/dual-read where needed.
- Operational readiness review templates; runbooks for top incidents.

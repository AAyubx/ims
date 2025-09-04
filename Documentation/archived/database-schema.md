````markdown
# Database Schema
_Last updated: 2025-09-03_

This consolidates the SaaS-mode DDL and schema notes into a single reference.

Key points:
- Tenancy model: single DB, row-scoped by `tenant_id` with composite unique keys.
- Use `utf8mb4` and UTC timestamps.
- Important tables: `tenant`, `user_account`, `role`, `user_role`, `location`, `item`, `item_variant`, `inventory_ledger`, `stock_summary`, `purchase_order`, `receipt`.

DDL examples and indexing guidance are preserved from the original schema doc. See `src/main/resources/db/migration/` for actual migrations.

Updates (2025-09-03): Several columns previously defined as `ENUM` were converted to `VARCHAR` via forward Flyway migrations (V4..V8) to match JPA enum string mappings.

````

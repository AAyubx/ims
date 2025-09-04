# MySQL DDL — SaaS Multi‑Tenant Inventory Platform

_Last updated: 2025-08-31 12:38 UTC_

> **Tenancy model:** single database, **row‑scoped by `tenant_id`** on every business table.  
> Enforce isolation in the application layer (JWT claim → `tenant_id`) and via **composite unique keys** including `tenant_id`.  
> All tables use **InnoDB**, **utf8mb4**, and timestamps in UTC.

---

## Session Defaults

```sql
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET time_zone = '+00:00';
```

## 1) Core: Tenants, Users, Roles, Orgs & Locations

```sql
CREATE TABLE tenant (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  code          VARCHAR(64) NOT NULL UNIQUE,
  name          VARCHAR(255) NOT NULL,
  status        ENUM('ACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE user_account (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  email         VARCHAR(320) NOT NULL,
  display_name  VARCHAR(255) NOT NULL,
  password_hash VARBINARY(255) NULL, -- if using internal auth; otherwise OIDC
  status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_user_email_tenant (tenant_id, email),
  CONSTRAINT fk_user_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE role (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(64) NOT NULL,
  name        VARCHAR(128) NOT NULL,
  UNIQUE KEY uq_role_code (code)
) ENGINE=InnoDB;

CREATE TABLE user_role (
  user_id    BIGINT NOT NULL,
  role_id    BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE location (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  code          VARCHAR(64) NOT NULL,
  name          VARCHAR(255) NOT NULL,
  type          ENUM('STORE','WAREHOUSE') NOT NULL,
  status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_loc_code_tenant (tenant_id, code),
  KEY ix_loc_tenant (tenant_id),
  CONSTRAINT fk_loc_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

## 2) Catalog: Categories, Items, Variants, Attributes

```sql
CREATE TABLE category (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  code          VARCHAR(64) NOT NULL,
  name          VARCHAR(255) NOT NULL,
  parent_id     BIGINT NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_cat_code_tenant (tenant_id, code),
  KEY ix_cat_tenant (tenant_id),
  CONSTRAINT fk_cat_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE item (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  sku           VARCHAR(64) NOT NULL,
  name          VARCHAR(255) NOT NULL,
  brand         VARCHAR(128),
  category_id   BIGINT NULL,
  status        ENUM('DRAFT','ACTIVE','DISCONTINUED') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_item_sku_tenant (tenant_id, sku),
  KEY ix_item_cat (category_id),
  KEY ix_item_tenant (tenant_id),
  CONSTRAINT fk_item_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE item_variant (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  item_id       BIGINT NOT NULL,
  variant_sku   VARCHAR(64) NOT NULL,
  attributes_json JSON NULL, -- size/color, etc.
  status        ENUM('ACTIVE','DISCONTINUED') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_variant_sku_tenant (tenant_id, variant_sku),
  KEY ix_variant_item (item_id),
  KEY ix_variant_tenant (tenant_id),
  CONSTRAINT fk_variant_item FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
  CONSTRAINT fk_variant_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

## 3) Inventory: Ledger, Summary, Serial/Lot (optional)

```sql
CREATE TABLE inventory_ledger (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  location_id   BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  ref_type      ENUM('RECEIPT','SHIPMENT','TRANSFER','ADJUSTMENT','COUNT') NOT NULL,
  ref_id        VARCHAR(64) NOT NULL,
  qty_delta     INT NOT NULL, -- positive/negative
  reason_code   VARCHAR(32),
  ts            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  user_id       BIGINT NULL,
  -- Idempotency to prevent duplicates from integrations:
  UNIQUE KEY uq_ledger_idem (tenant_id, ref_type, ref_id, variant_id, location_id),
  KEY ix_led_loc_sku_ts (tenant_id, location_id, variant_id, ts),
  CONSTRAINT fk_led_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_led_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE RESTRICT,
  CONSTRAINT fk_led_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE stock_summary (
  tenant_id     BIGINT NOT NULL,
  location_id   BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  on_hand       INT NOT NULL DEFAULT 0,
  reserved      INT NOT NULL DEFAULT 0,
  available     INT AS (on_hand - reserved) STORED,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (tenant_id, location_id, variant_id),
  CONSTRAINT fk_sum_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_sum_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE RESTRICT,
  CONSTRAINT fk_sum_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Optional: serial/lot tracking
CREATE TABLE lot (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  lot_code      VARCHAR(64) NOT NULL,
  expiry_date   DATE NULL,
  UNIQUE KEY uq_lot_code (tenant_id, variant_id, lot_code),
  CONSTRAINT fk_lot_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_lot_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

## 4) Suppliers, Purchasing & Receiving

```sql
CREATE TABLE supplier (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  code          VARCHAR(64) NOT NULL,
  name          VARCHAR(255) NOT NULL,
  contact_email VARCHAR(320),
  status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_supplier_code (tenant_id, code),
  CONSTRAINT fk_supplier_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE purchase_order (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  supplier_id   BIGINT NOT NULL,
  location_id   BIGINT NOT NULL, -- deliver-to
  code          VARCHAR(64) NOT NULL,
  status        ENUM('DRAFT','APPROVED','PARTIAL','RECEIVED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
  eta_date      DATE NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_po_code (tenant_id, code),
  KEY ix_po_supplier (supplier_id),
  CONSTRAINT fk_po_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id) ON DELETE RESTRICT,
  CONSTRAINT fk_po_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE purchase_order_line (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  po_id         BIGINT NOT NULL,
  tenant_id     BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  qty_ordered   INT NOT NULL,
  unit_cost     DECIMAL(12,4) NOT NULL DEFAULT 0,
  qty_received  INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_pol_po FOREIGN KEY (po_id) REFERENCES purchase_order(id) ON DELETE CASCADE,
  CONSTRAINT fk_pol_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE RESTRICT,
  CONSTRAINT fk_pol_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  UNIQUE KEY uq_pol (po_id, variant_id)
) ENGINE=InnoDB;

CREATE TABLE receipt (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  po_id         BIGINT NULL,
  location_id   BIGINT NOT NULL,
  received_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by    BIGINT NULL,
  CONSTRAINT fk_receipt_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_receipt_po FOREIGN KEY (po_id) REFERENCES purchase_order(id) ON DELETE SET NULL,
  CONSTRAINT fk_receipt_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE receipt_line (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  receipt_id    BIGINT NOT NULL,
  tenant_id     BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  qty_received  INT NOT NULL,
  lot_id        BIGINT NULL,
  CONSTRAINT fk_rl_receipt FOREIGN KEY (receipt_id) REFERENCES receipt(id) ON DELETE CASCADE,
  CONSTRAINT fk_rl_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE RESTRICT,
  CONSTRAINT fk_rl_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_rl_lot FOREIGN KEY (lot_id) REFERENCES lot(id) ON DELETE SET NULL
) ENGINE=InnoDB;
```

## 5) Transfers & Adjustments

```sql
CREATE TABLE transfer_order (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  code          VARCHAR(64) NOT NULL,
  source_loc_id BIGINT NOT NULL,
  dest_loc_id   BIGINT NOT NULL,
  status        ENUM('DRAFT','DISPATCHED','RECEIVED','CANCELLED') NOT NULL DEFAULT 'DRAFT',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_to_code (tenant_id, code),
  CONSTRAINT fk_to_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_to_src FOREIGN KEY (source_loc_id) REFERENCES location(id) ON DELETE RESTRICT,
  CONSTRAINT fk_to_dest FOREIGN KEY (dest_loc_id) REFERENCES location(id) ON DELETE RESTRICT,
  CHECK (source_loc_id <> dest_loc_id)
) ENGINE=InnoDB;

CREATE TABLE transfer_line (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  transfer_id   BIGINT NOT NULL,
  tenant_id     BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  qty           INT NOT NULL,
  CONSTRAINT fk_tl_transfer FOREIGN KEY (transfer_id) REFERENCES transfer_order(id) ON DELETE CASCADE,
  CONSTRAINT fk_tl_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE RESTRICT,
  CONSTRAINT fk_tl_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  UNIQUE KEY uq_tl (transfer_id, variant_id)
) ENGINE=InnoDB;

CREATE TABLE adjustment (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  location_id   BIGINT NOT NULL,
  reason_code   VARCHAR(32) NOT NULL,
  notes         VARCHAR(512),
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by    BIGINT NULL,
  CONSTRAINT fk_adj_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_adj_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE adjustment_line (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  adjustment_id BIGINT NOT NULL,
  tenant_id     BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  qty_delta     INT NOT NULL,
  CONSTRAINT fk_al_adj FOREIGN KEY (adjustment_id) REFERENCES adjustment(id) ON DELETE CASCADE,
  CONSTRAINT fk_al_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE RESTRICT,
  CONSTRAINT fk_al_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  UNIQUE KEY uq_al (adjustment_id, variant_id)
) ENGINE=InnoDB;
```

## 6) Pricing & Promotions (minimal)

```sql
CREATE TABLE price_list (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  code          VARCHAR(64) NOT NULL,
  name          VARCHAR(255) NOT NULL,
  currency      CHAR(3) NOT NULL,
  valid_from    DATE NULL,
  valid_to      DATE NULL,
  UNIQUE KEY uq_pl_code (tenant_id, code),
  CONSTRAINT fk_pl_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE price_list_item (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  price_list_id BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  price         DECIMAL(12,4) NOT NULL,
  UNIQUE KEY uq_pli (price_list_id, variant_id),
  CONSTRAINT fk_pli_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_pli_pl FOREIGN KEY (price_list_id) REFERENCES price_list(id) ON DELETE CASCADE,
  CONSTRAINT fk_pli_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
```

## 7) Integration & Extensibility

```sql
-- Outbox for reliable event publishing (Debezium-friendly)
CREATE TABLE events_outbox (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id   VARCHAR(64) NOT NULL,
  event_type     VARCHAR(64) NOT NULL,
  payload        JSON NOT NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published      TINYINT(1) NOT NULL DEFAULT 0,
  KEY ix_outbox_pub (published, created_at),
  KEY ix_outbox_tenant (tenant_id),
  CONSTRAINT fk_outbox_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE webhook_subscription (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  event_type    VARCHAR(64) NOT NULL,
  target_url    VARCHAR(1024) NOT NULL,
  secret        VARCHAR(255) NULL,
  status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uq_wh (tenant_id, event_type, target_url),
  CONSTRAINT fk_wh_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

## 8) Indexing & Performance Notes

- Add **covering indexes** for frequent queries (e.g., `(tenant_id, sku)` / `(tenant_id, location_id, variant_id)`).
- Consider **partitioning `inventory_ledger` by RANGE on `ts`** for high‑volume tenants:

```sql
ALTER TABLE inventory_ledger
PARTITION BY RANGE (TO_DAYS(ts)) (
  PARTITION p2025q3 VALUES LESS THAN (TO_DAYS('2025-10-01')),
  PARTITION pmax VALUES LESS THAN MAXVALUE
);
```

## 9) Referential Integrity & Cascades

- Most child tables use `ON DELETE CASCADE` from parents within the same bounded context.
- Cross‑context FKs use `ON DELETE RESTRICT` to preserve data integrity.

## 10) Seed & System Data (examples)

```sql
INSERT INTO role(code, name) VALUES
 ('ADMIN','Administrator'),
 ('MANAGER','Store/Warehouse Manager'),
 ('CLERK','Operations Clerk'),
 ('VIEWER','Read-only');

INSERT INTO tenant(code, name) VALUES ('demo', 'Demo Tenant');
```

---

### Notes for SaaS Hardening

- Add **row-level filters** in the application for every query by `tenant_id`.
- Use **read replicas** for reporting; route via ProxySQL/HAProxy.
- Encrypt sensitive columns at rest if needed; restrict PII exposure.
- Implement **per-tenant quotas** and rate-limit policies at the API gateway.

## Updates (2025-09-03)

Important schema and migration notes:

- Several columns in the live development database that were originally defined as MySQL `ENUM` (for example: `tenant.status`, `user_account.status`, `location.type`, `location.status`, and `audit_log.action_type`) were converted to `VARCHAR` via forward Flyway migrations (V4 through V8). The conversions were necessary because the application maps enums using JPA's `@Enumerated(EnumType.STRING)`, which expects string-based columns (VARCHAR) and caused Hibernate schema-validation to fail when the columns remained ENUM.
- Recommendation: for new schema work, prefer using `VARCHAR` (with a reasonable length) for columns that map to Java enums with `EnumType.STRING`. If you must use `ENUM` at the DDL level, ensure the JPA mapping and Hibernate validation are adjusted accordingly, or skip validation for that table.
- Migration policy reminder: do not modify already-applied migration files. To change a live database schema, add a new forward migration (e.g., `V9__...sql`) that performs the necessary `ALTER TABLE ... MODIFY COLUMN ... VARCHAR(...)` and run Flyway `migrate`.

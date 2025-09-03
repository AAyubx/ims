-- schema_full.sql
-- Generated: 2025-08-31 12:54 UTC
-- Standalone executable schema creator for inventory_saas database

CREATE DATABASE IF NOT EXISTS inventory_saas
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE inventory_saas;

-- Session defaults
SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET time_zone = '+00:00';


SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS webhook_subscription CASCADE;
DROP TABLE IF EXISTS events_outbox CASCADE;
DROP TABLE IF EXISTS price_list_item CASCADE;
DROP TABLE IF EXISTS price_list CASCADE;
DROP TABLE IF EXISTS adjustment_line CASCADE;
DROP TABLE IF EXISTS adjustment CASCADE;
DROP TABLE IF EXISTS transfer_line CASCADE;
DROP TABLE IF EXISTS transfer_order CASCADE;
DROP TABLE IF EXISTS receipt_line CASCADE;
DROP TABLE IF EXISTS receipt CASCADE;
DROP TABLE IF EXISTS purchase_order_line CASCADE;
DROP TABLE IF EXISTS purchase_order CASCADE;
DROP TABLE IF EXISTS supplier CASCADE;
DROP TABLE IF EXISTS lot CASCADE;
DROP TABLE IF EXISTS stock_summary CASCADE;
DROP TABLE IF EXISTS inventory_ledger CASCADE;
DROP TABLE IF EXISTS item_variant CASCADE;
DROP TABLE IF EXISTS item CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS location CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS user_account CASCADE;
DROP TABLE IF EXISTS tenant CASCADE;
SET FOREIGN_KEY_CHECKS = 1;

-- 1) Core: tenants, users, roles, locations
CREATE TABLE tenant (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  code          VARCHAR(64) NOT NULL UNIQUE,
  name          VARCHAR(255) NOT NULL,
  status        ENUM('ACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_account (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  email         VARCHAR(320) NOT NULL,
  display_name  VARCHAR(255) NOT NULL,
  password_hash VARBINARY(255) NULL,
  status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_user_email_tenant (tenant_id, email),
  CONSTRAINT fk_user_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE role (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(64) NOT NULL,
  name        VARCHAR(128) NOT NULL,
  UNIQUE KEY uq_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE user_role (
  user_id    BIGINT NOT NULL,
  role_id    BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 2) Catalog: categories, items, variants
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE item_variant (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  item_id       BIGINT NOT NULL,
  variant_sku   VARCHAR(64) NOT NULL,
  attributes_json JSON NULL,
  status        ENUM('ACTIVE','DISCONTINUED') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_variant_sku_tenant (tenant_id, variant_sku),
  KEY ix_variant_item (item_id),
  KEY ix_variant_tenant (tenant_id),
  CONSTRAINT fk_variant_item FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
  CONSTRAINT fk_variant_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 3) Inventory: ledger, summary, lots
CREATE TABLE inventory_ledger (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  location_id   BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  ref_type      ENUM('RECEIPT','SHIPMENT','TRANSFER','ADJUSTMENT','COUNT') NOT NULL,
  ref_id        VARCHAR(64) NOT NULL,
  qty_delta     INT NOT NULL,
  reason_code   VARCHAR(32),
  ts            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  user_id       BIGINT NULL,
  UNIQUE KEY uq_ledger_idem (tenant_id, ref_type, ref_id, variant_id, location_id),
  KEY ix_led_loc_sku_ts (tenant_id, location_id, variant_id, ts),
  CONSTRAINT fk_led_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_led_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE RESTRICT,
  CONSTRAINT fk_led_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE lot (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  variant_id    BIGINT NOT NULL,
  lot_code      VARCHAR(64) NOT NULL,
  expiry_date   DATE NULL,
  UNIQUE KEY uq_lot_code (tenant_id, variant_id, lot_code),
  CONSTRAINT fk_lot_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_lot_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 4) Suppliers, purchasing, receiving
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE purchase_order (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  supplier_id   BIGINT NOT NULL,
  location_id   BIGINT NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 5) Transfers & adjustments
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 6) Pricing
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 7) Integration: outbox & webhooks
CREATE TABLE events_outbox (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id      BIGINT NOT NULL,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id   VARCHAR(64) NOT NULL,
  event_type     VARCHAR(64) NOT NULL,
  payload        JSON NOT NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published      TINYINT(1) NOT NULL DEFAULT 0,
  KEY ix_outbox_pub (published, created_at),
  KEY ix_outbox_tenant (tenant_id),
  CONSTRAINT fk_outbox_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE webhook_subscription (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id     BIGINT NOT NULL,
  event_type    VARCHAR(64) NOT NULL,
  target_url    VARCHAR(1024) NOT NULL,
  secret        VARCHAR(255) NULL,
  status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uq_wh (tenant_id, event_type, target_url(191)),
  CONSTRAINT fk_wh_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Seed data
INSERT INTO role(code, name) VALUES
 ('ADMIN','Administrator'),
 ('MANAGER','Store/Warehouse Manager'),
 ('CLERK','Operations Clerk'),
 ('VIEWER','Read-only');

INSERT INTO tenant(code, name, status) VALUES ('demo', 'Demo Tenant', 'ACTIVE');


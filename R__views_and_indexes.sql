-- R__views_and_indexes.sql
-- Repeatable migration: helper views, summary tables, and indexes
-- Generated: 2025-08-31 13:00 UTC
-- Updated: 2025-09-03 - Added security and audit indexes

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET time_zone = '+00:00';

-- ===== Views =====

-- Items with category
CREATE OR REPLACE VIEW v_items_with_category AS
SELECT i.tenant_id, i.id AS item_id, i.sku, i.name AS item_name, i.brand,
       c.id AS category_id, c.code AS category_code, c.name AS category_name
FROM item i
LEFT JOIN category c ON c.id = i.category_id;

-- Variants current price
CREATE OR REPLACE VIEW v_variant_price_current AS
SELECT pli.tenant_id, pli.variant_id, pli.price
FROM price_list_item pli
JOIN price_list pl ON pl.id = pli.price_list_id
WHERE (pl.valid_from IS NULL OR pl.valid_from <= CURRENT_DATE())
  AND (pl.valid_to   IS NULL OR pl.valid_to   >= CURRENT_DATE());

-- Stock by location with names
CREATE OR REPLACE VIEW v_stock_by_location AS
SELECT ss.tenant_id, loc.code AS location_code, loc.name AS location_name,
       iv.variant_sku, i.sku AS item_sku, i.name AS item_name,
       ss.on_hand, ss.reserved, ss.available
FROM stock_summary ss
JOIN location loc ON loc.id = ss.location_id AND loc.tenant_id = ss.tenant_id
JOIN item_variant iv ON iv.id = ss.variant_id AND iv.tenant_id = ss.tenant_id
JOIN item i ON i.id = iv.item_id AND i.tenant_id = ss.tenant_id;

-- Daily inventory movement (by calendar day)
CREATE OR REPLACE VIEW v_inventory_movement_daily AS
SELECT tenant_id,
       DATE(ts) AS day,
       location_id,
       variant_id,
       SUM(qty_delta) AS qty_delta
FROM inventory_ledger
GROUP BY tenant_id, DATE(ts), location_id, variant_id;

-- SKU availability (sum across locations)
CREATE OR REPLACE VIEW v_sku_availability AS
SELECT ss.tenant_id, iv.variant_sku, SUM(ss.available) AS total_available
FROM stock_summary ss
JOIN item_variant iv ON iv.id = ss.variant_id AND iv.tenant_id = ss.tenant_id
GROUP BY ss.tenant_id, iv.variant_sku;

-- ===== "Materialized" helper table + refresh procedure =====
CREATE TABLE IF NOT EXISTS inventory_ledger_daily (
  tenant_id   BIGINT NOT NULL,
  day         DATE   NOT NULL,
  location_id BIGINT NOT NULL,
  variant_id  BIGINT NOT NULL,
  qty_delta   INT    NOT NULL,
  PRIMARY KEY (tenant_id, day, location_id, variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS refresh_inventory_ledger_daily(IN p_days INT)
BEGIN
  DECLARE v_from DATE;
  SET v_from = DATE_SUB(CURRENT_DATE(), INTERVAL p_days DAY);

  REPLACE INTO inventory_ledger_daily (tenant_id, day, location_id, variant_id, qty_delta)
  SELECT tenant_id, DATE(ts) AS day, location_id, variant_id, SUM(qty_delta)
  FROM inventory_ledger
  WHERE ts >= v_from
  GROUP BY tenant_id, DATE(ts), location_id, variant_id;
END$$
DELIMITER ;

CREATE EVENT IF NOT EXISTS ev_refresh_inventory_ledger_daily
ON SCHEDULE EVERY 1 DAY STARTS (CURRENT_DATE() + INTERVAL 1 HOUR)
DO
  CALL refresh_inventory_ledger_daily(30);

-- ===== Indexes (drop/create idempotently) =====

-- item(sku) composite with tenant
DROP INDEX IF EXISTS ix_item_tenant_sku ON item;
CREATE INDEX ix_item_tenant_sku ON item (tenant_id, sku);

-- item_variant(tenant, variant_sku)
DROP INDEX IF EXISTS ix_variant_tenant_sku ON item_variant;
CREATE INDEX ix_variant_tenant_sku ON item_variant (tenant_id, variant_sku);

-- inventory_ledger composite access paths
DROP INDEX IF EXISTS ix_led_tenant_ts ON inventory_ledger;
CREATE INDEX ix_led_tenant_ts ON inventory_ledger (tenant_id, ts);

DROP INDEX IF EXISTS ix_led_tenant_loc_sku ON inventory_ledger;
CREATE INDEX ix_led_tenant_loc_sku ON inventory_ledger (tenant_id, location_id, variant_id);

-- stock_summary add by-tenant lookup
DROP INDEX IF EXISTS ix_sum_tenant ON stock_summary;
CREATE INDEX ix_sum_tenant ON stock_summary (tenant_id);

-- purchase_order_line lookup by variant
DROP INDEX IF EXISTS ix_pol_variant ON purchase_order_line;
CREATE INDEX ix_pol_variant ON purchase_order_line (variant_id);

-- ===== Security and Audit Indexes =====

-- User security indexes
DROP INDEX IF EXISTS ix_user_failed_attempts ON user_account;
CREATE INDEX ix_user_failed_attempts ON user_account (failed_login_attempts, account_locked_until);

DROP INDEX IF EXISTS ix_user_password_expiry ON user_account;
CREATE INDEX ix_user_password_expiry ON user_account (password_expires_at, must_change_password);

-- Login attempts performance indexes
DROP INDEX IF EXISTS ix_login_attempts_recent ON login_attempts;
CREATE INDEX ix_login_attempts_recent ON login_attempts (email, attempted_at DESC, success);

-- Session management indexes
DROP INDEX IF EXISTS ix_sessions_cleanup ON user_sessions;
CREATE INDEX ix_sessions_cleanup ON user_sessions (expires_at, is_active);

DROP INDEX IF EXISTS ix_sessions_user_active ON user_sessions;
CREATE INDEX ix_sessions_user_active ON user_sessions (user_id, is_active, last_accessed_at);

-- Audit log performance indexes
DROP INDEX IF EXISTS ix_audit_recent_actions ON audit_log;
CREATE INDEX ix_audit_recent_actions ON audit_log (tenant_id, action_type, created_at DESC);

DROP INDEX IF EXISTS ix_audit_entity_changes ON audit_log;
CREATE INDEX ix_audit_entity_changes ON audit_log (entity_type, entity_id, created_at DESC);

-- System config lookup index
DROP INDEX IF EXISTS ix_config_key ON system_config;
CREATE INDEX ix_config_key ON system_config (config_key);

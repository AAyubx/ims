-- V9: Consolidate enum->varchar conversions for columns changed in V4..V8
-- This file groups the ALTER statements previously added in multiple migrations
-- Keeping the previous V4..V8 files intact to preserve Flyway history. This
-- consolidated migration is safe (idempotent when the column is already VARCHAR)
-- for new environments and provides a single place to see all enum->varchar changes.

-- Convert audit_log.action_type to VARCHAR(32)
ALTER TABLE audit_log
  MODIFY COLUMN action_type VARCHAR(32) NOT NULL;

-- Convert location.status to VARCHAR(255)
ALTER TABLE location
  MODIFY COLUMN status VARCHAR(255) NOT NULL;

-- Convert location.type to VARCHAR(255)
ALTER TABLE location
  MODIFY COLUMN `type` VARCHAR(255) NOT NULL;

-- Convert tenant.status to VARCHAR(255)
ALTER TABLE tenant
  MODIFY COLUMN `status` VARCHAR(255) NOT NULL;

-- Convert user_account.status to VARCHAR(255)
ALTER TABLE user_account
  MODIFY COLUMN `status` VARCHAR(255) NOT NULL;

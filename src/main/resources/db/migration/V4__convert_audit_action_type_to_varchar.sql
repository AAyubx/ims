-- V4: Convert audit_log.action_type from ENUM to VARCHAR so it matches JPA @Enumerated(EnumType.STRING)
-- This migration updates the physical column type for existing databases.
-- MySQL: MODIFY or ALTER COLUMN to change the column type. Keep NOT NULL if required.

ALTER TABLE audit_log
  MODIFY COLUMN action_type VARCHAR(32) NOT NULL;

-- If there is a default or index that depends on the ENUM, adjust below (none expected in current schema).

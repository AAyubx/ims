-- V8: Convert user_account.status from ENUM to VARCHAR so it matches JPA @Enumerated(EnumType.STRING)
-- This migration updates the physical column type for existing databases.
-- MySQL: MODIFY column to change the column type. Keep NOT NULL if required.

ALTER TABLE user_account
  MODIFY COLUMN `status` VARCHAR(255) NOT NULL;

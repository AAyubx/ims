-- V6: Convert location.type from ENUM to VARCHAR so it matches JPA @Enumerated(EnumType.STRING)
-- This migration updates the physical column type for existing databases.
-- MySQL: MODIFY column to change the column type. Keep NOT NULL if required.

ALTER TABLE location
  MODIFY COLUMN `type` VARCHAR(255) NOT NULL;

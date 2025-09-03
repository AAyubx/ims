-- V5: Convert location.status from ENUM to VARCHAR so it matches JPA mapping
-- This migration updates the physical column type for existing databases.
-- MySQL: MODIFY column to change the column type. Keep NOT NULL if required.

ALTER TABLE location
  MODIFY COLUMN status VARCHAR(255) NOT NULL;

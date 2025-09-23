-- V23__convert_item_status_enum_to_varchar.sql
-- Convert item status column from ENUM to VARCHAR to match JPA entity configuration
-- This aligns with the V9 migration pattern that converted other enum columns

-- =============================================================================
-- CONVERT ITEM STATUS ENUM TO VARCHAR
-- =============================================================================

-- Convert the status column from ENUM('DRAFT','ACTIVE','DISCONTINUED') to VARCHAR(20)
-- This matches the @Enumerated(EnumType.STRING) configuration in Item.java entity
ALTER TABLE item MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Verify data integrity after conversion
-- Ensure all existing values are preserved correctly
UPDATE item SET status = 'ACTIVE' WHERE status = '';
UPDATE item SET status = 'DRAFT' WHERE status IS NULL;
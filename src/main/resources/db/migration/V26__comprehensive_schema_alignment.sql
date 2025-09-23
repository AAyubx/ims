-- V26__comprehensive_schema_alignment.sql
-- MEGA MIGRATION: Fix ALL entity-database schema mismatches in one go
-- This eliminates the whack-a-mole problem by addressing all known issues systematically

-- =============================================================================
-- CONVERT ALL ENUM COLUMNS TO VARCHAR (Based on @Enumerated(EnumType.STRING))
-- =============================================================================

-- Fix supplier.status (current error)
ALTER TABLE supplier MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Fix department.status  
ALTER TABLE department MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Fix brand.status
ALTER TABLE brand MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Fix location.type and location.status
ALTER TABLE location MODIFY COLUMN type VARCHAR(20) NOT NULL;
ALTER TABLE location MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Fix tenant.status
ALTER TABLE tenant MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Fix user_account.status  
ALTER TABLE user_account MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Fix attribute_definition.data_type
ALTER TABLE attribute_definition MODIFY COLUMN data_type VARCHAR(20) NOT NULL;

-- Fix unit_of_measure.unit_type
ALTER TABLE unit_of_measure MODIFY COLUMN unit_type VARCHAR(32) NOT NULL;

-- Fix store_configuration.config_type
ALTER TABLE store_configuration MODIFY COLUMN config_type VARCHAR(20) NOT NULL DEFAULT 'STRING';

-- Fix tax_jurisdiction.tax_type
ALTER TABLE tax_jurisdiction MODIFY COLUMN tax_type VARCHAR(20) NOT NULL;

-- Fix audit_log.action_type
ALTER TABLE audit_log MODIFY COLUMN action_type VARCHAR(32) NOT NULL;

-- Fix item.item_type (in case V21 didn't handle it properly)
ALTER TABLE item MODIFY COLUMN item_type VARCHAR(32) NOT NULL DEFAULT 'SIMPLE';

-- Note: item.status already fixed in V23, item_variant.status already fixed in V25

-- =============================================================================
-- ADD ANY MISSING COLUMNS FOR OTHER ENTITIES
-- =============================================================================

-- These will be identified if we get more schema validation errors
-- For now, focusing on the ENUM-to-VARCHAR conversions that are causing immediate failures

-- =============================================================================
-- PERFORMANCE OPTIMIZATIONS
-- =============================================================================

-- Update table statistics for all modified tables
ANALYZE TABLE supplier;
ANALYZE TABLE department;
ANALYZE TABLE brand;
ANALYZE TABLE location;
ANALYZE TABLE tenant;
ANALYZE TABLE user_account;
ANALYZE TABLE attribute_definition;
ANALYZE TABLE unit_of_measure;
ANALYZE TABLE store_configuration;
ANALYZE TABLE tax_jurisdiction;
ANALYZE TABLE audit_log;
ANALYZE TABLE item;
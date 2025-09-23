-- V27__add_missing_supplier_audit_columns.sql
-- Add missing audit columns to supplier table

-- =============================================================================
-- ADD MISSING AUDIT COLUMNS TO SUPPLIER TABLE
-- =============================================================================

-- Add missing created_by and updated_by columns
ALTER TABLE supplier 
    ADD COLUMN created_by BIGINT AFTER updated_at,
    ADD COLUMN updated_by BIGINT AFTER created_by;

-- Add foreign key constraints for audit fields
ALTER TABLE supplier 
    ADD CONSTRAINT fk_supplier_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_supplier_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL;

-- Add indexes for performance
CREATE INDEX idx_supplier_created_by ON supplier(created_by);
CREATE INDEX idx_supplier_updated_by ON supplier(updated_by);
-- V30__add_barcode_business_constraints.sql
-- Add business rule constraints for barcode management

-- Add check constraints for valid enum values (MySQL 8.0.16+)
ALTER TABLE item_barcode 
  ADD CONSTRAINT chk_barcode_type 
  CHECK (barcode_type IN ('UPC_A', 'UPC_E', 'EAN_13', 'EAN_8', 'ITF_14', 'GS1_128', 'CODE_128', 'CODE_39', 'DATAMATRIX_GS1', 'QR_GS1_LINK')),
  ADD CONSTRAINT chk_pack_level 
  CHECK (pack_level IN ('EACH', 'INNER', 'CASE', 'PALLET') OR pack_level IS NULL),
  ADD CONSTRAINT chk_status 
  CHECK (status IN ('RESERVED', 'ACTIVE', 'DEPRECATED', 'BLOCKED'));

-- Performance indexes for common query patterns
CREATE INDEX idx_barcode_uom_pack ON item_barcode(tenant_id, uom_id, pack_level);
CREATE INDEX idx_barcode_created_at ON item_barcode(tenant_id, created_at);

-- Business rule: Only one primary barcode per variant/UoM/pack combination
-- Note: This constraint is complex due to NULL handling, will be enforced in application logic
-- CREATE UNIQUE INDEX idx_primary_per_variant ON item_barcode(tenant_id, variant_id, COALESCE(uom_id, 0), COALESCE(pack_level, 'EACH'), is_primary) WHERE is_primary = TRUE;
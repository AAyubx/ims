-- V21__consolidate_item_schema_fixes.sql  
-- Consolidate and optimize all item table schema fixes
-- MySQL-compatible version without IF NOT EXISTS for columns

-- =============================================================================
-- ITEM TABLE ENHANCEMENTS
-- =============================================================================

-- Add missing columns to item table (skip if already exist from previous migrations)

-- Core item attributes
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN short_name VARCHAR(128) AFTER name;' ELSE 'SELECT ''Column short_name already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'short_name');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN description TEXT AFTER short_name;' ELSE 'SELECT ''Column description already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'description');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Organizational structure
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN department_id BIGINT AFTER category_id;' ELSE 'SELECT ''Column department_id already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'department_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN brand_id BIGINT AFTER department_id;' ELSE 'SELECT ''Column brand_id already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'brand_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN item_type VARCHAR(32) NOT NULL DEFAULT ''SIMPLE'' AFTER brand_id;' ELSE 'SELECT ''Column item_type already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'item_type');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Unit of Measure relationships
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN base_uom_id BIGINT AFTER item_type;' ELSE 'SELECT ''Column base_uom_id already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'base_uom_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN sell_uom_id BIGINT AFTER base_uom_id;' ELSE 'SELECT ''Column sell_uom_id already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'sell_uom_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN buy_uom_id BIGINT AFTER sell_uom_id;' ELSE 'SELECT ''Column buy_uom_id already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'buy_uom_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Tax and regulatory  
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN tax_class VARCHAR(32) AFTER buy_uom_id;' ELSE 'SELECT ''Column tax_class already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'tax_class');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN hs_code VARCHAR(32) AFTER tax_class;' ELSE 'SELECT ''Column hs_code already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'hs_code');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Note: country_of_origin was added in V19, should already exist

-- Inventory management
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN is_serialized BOOLEAN NOT NULL DEFAULT FALSE AFTER country_of_origin;' ELSE 'SELECT ''Column is_serialized already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'is_serialized');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN is_lot_tracked BOOLEAN NOT NULL DEFAULT FALSE AFTER is_serialized;' ELSE 'SELECT ''Column is_lot_tracked already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'is_lot_tracked');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN shelf_life_days INT AFTER is_lot_tracked;' ELSE 'SELECT ''Column shelf_life_days already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'shelf_life_days');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN safety_stock_default INT NOT NULL DEFAULT 0 AFTER shelf_life_days;' ELSE 'SELECT ''Column safety_stock_default already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'safety_stock_default');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN reorder_point_default INT NOT NULL DEFAULT 0 AFTER safety_stock_default;' ELSE 'SELECT ''Column reorder_point_default already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'reorder_point_default');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN reorder_quantity_default INT NOT NULL DEFAULT 0 AFTER reorder_point_default;' ELSE 'SELECT ''Column reorder_quantity_default already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'reorder_quantity_default');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Cost management
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN standard_cost DECIMAL(12,4) AFTER reorder_quantity_default;' ELSE 'SELECT ''Column standard_cost already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'standard_cost');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN last_cost DECIMAL(12,4) AFTER standard_cost;' ELSE 'SELECT ''Column last_cost already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'last_cost');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN average_cost DECIMAL(12,4) AFTER last_cost;' ELSE 'SELECT ''Column average_cost already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'average_cost');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Note: base_price was added in V18, should already exist

-- SEO and marketing
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN meta_title VARCHAR(255) AFTER base_price;' ELSE 'SELECT ''Column meta_title already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'meta_title');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN meta_description TEXT AFTER meta_title;' ELSE 'SELECT ''Column meta_description already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'meta_description');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN search_keywords TEXT AFTER meta_description;' ELSE 'SELECT ''Column search_keywords already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'search_keywords');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Audit fields
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN created_by BIGINT AFTER updated_at;' ELSE 'SELECT ''Column created_by already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'created_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD COLUMN updated_by BIGINT AFTER created_by;' ELSE 'SELECT ''Column updated_by already exists'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item' AND column_name = 'updated_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add foreign key constraints (MySQL supports ADD CONSTRAINT IF NOT EXISTS in MySQL 8.0.19+)
-- For compatibility, we'll check if constraint exists first

-- Check and add department foreign key
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD CONSTRAINT fk_item_department FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_item_department already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'item' AND constraint_name = 'fk_item_department');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Check and add brand foreign key
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD CONSTRAINT fk_item_brand FOREIGN KEY (brand_id) REFERENCES brand(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_item_brand already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'item' AND constraint_name = 'fk_item_brand');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Check and add UOM foreign keys
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD CONSTRAINT fk_item_base_uom FOREIGN KEY (base_uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_item_base_uom already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'item' AND constraint_name = 'fk_item_base_uom');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD CONSTRAINT fk_item_sell_uom FOREIGN KEY (sell_uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_item_sell_uom already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'item' AND constraint_name = 'fk_item_sell_uom');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD CONSTRAINT fk_item_buy_uom FOREIGN KEY (buy_uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_item_buy_uom already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'item' AND constraint_name = 'fk_item_buy_uom');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Check and add audit foreign keys  
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD CONSTRAINT fk_item_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_item_created_by already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'item' AND constraint_name = 'fk_item_created_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item ADD CONSTRAINT fk_item_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_item_updated_by already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'item' AND constraint_name = 'fk_item_updated_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add optimized indexes for better query performance (MySQL-compatible conditional index creation)
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_department ON item(department_id);' ELSE 'SELECT ''Index idx_item_department already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_department');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_brand ON item(brand_id);' ELSE 'SELECT ''Index idx_item_brand already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_brand');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_type ON item(item_type);' ELSE 'SELECT ''Index idx_item_type already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_type');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_base_uom ON item(base_uom_id);' ELSE 'SELECT ''Index idx_item_base_uom already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_base_uom');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_serialized ON item(is_serialized);' ELSE 'SELECT ''Index idx_item_serialized already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_serialized');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_lot_tracked ON item(is_lot_tracked);' ELSE 'SELECT ''Index idx_item_lot_tracked already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_lot_tracked');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_status_type ON item(status, item_type);' ELSE 'SELECT ''Index idx_item_status_type already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_status_type');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_created_by ON item(created_by);' ELSE 'SELECT ''Index idx_item_created_by already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_created_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_updated_by ON item(updated_by);' ELSE 'SELECT ''Index idx_item_updated_by already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_updated_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================================
-- ITEM_VARIANT TABLE ENHANCEMENTS  
-- =============================================================================

-- Add missing columns to item_variant table
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item_variant ADD COLUMN name VARCHAR(255) AFTER variant_sku;' ELSE 'SELECT ''Column name already exists in item_variant'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item_variant' AND column_name = 'name');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item_variant ADD COLUMN average_cost DECIMAL(12,4) AFTER name;' ELSE 'SELECT ''Column average_cost already exists in item_variant'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item_variant' AND column_name = 'average_cost');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item_variant ADD COLUMN created_by BIGINT AFTER updated_at;' ELSE 'SELECT ''Column created_by already exists in item_variant'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item_variant' AND column_name = 'created_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item_variant ADD COLUMN updated_by BIGINT AFTER created_by;' ELSE 'SELECT ''Column updated_by already exists in item_variant'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'item_variant' AND column_name = 'updated_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add foreign key constraints for item_variant
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item_variant ADD CONSTRAINT fk_item_variant_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_item_variant_created_by already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'item_variant' AND constraint_name = 'fk_item_variant_created_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE item_variant ADD CONSTRAINT fk_item_variant_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_item_variant_updated_by already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'item_variant' AND constraint_name = 'fk_item_variant_updated_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add indexes for item_variant
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_variant_created_by ON item_variant(created_by);' ELSE 'SELECT ''Index idx_item_variant_created_by already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item_variant' AND index_name = 'idx_item_variant_created_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_variant_updated_by ON item_variant(updated_by);' ELSE 'SELECT ''Index idx_item_variant_updated_by already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item_variant' AND index_name = 'idx_item_variant_updated_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================================
-- CATEGORY TABLE ENHANCEMENTS
-- =============================================================================

-- Add missing columns to category table
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE category ADD COLUMN department_id BIGINT AFTER tenant_id;' ELSE 'SELECT ''Column department_id already exists in category'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'category' AND column_name = 'department_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE category ADD COLUMN description TEXT AFTER name;' ELSE 'SELECT ''Column description already exists in category'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'category' AND column_name = 'description');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE category ADD COLUMN created_by BIGINT AFTER updated_at;' ELSE 'SELECT ''Column created_by already exists in category'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'category' AND column_name = 'created_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE category ADD COLUMN updated_by BIGINT AFTER created_by;' ELSE 'SELECT ''Column updated_by already exists in category'';' END FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'category' AND column_name = 'updated_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add foreign key constraints for category
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE category ADD CONSTRAINT fk_category_department FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_category_department already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'category' AND constraint_name = 'fk_category_department');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE category ADD CONSTRAINT fk_category_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_category_created_by already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'category' AND constraint_name = 'fk_category_created_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'ALTER TABLE category ADD CONSTRAINT fk_category_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL;' ELSE 'SELECT ''Constraint fk_category_updated_by already exists'';' END FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'category' AND constraint_name = 'fk_category_updated_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add indexes for category
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_category_department ON category(department_id);' ELSE 'SELECT ''Index idx_category_department already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'category' AND index_name = 'idx_category_department');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_category_created_by ON category(created_by);' ELSE 'SELECT ''Index idx_category_created_by already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'category' AND index_name = 'idx_category_created_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_category_updated_by ON category(updated_by);' ELSE 'SELECT ''Index idx_category_updated_by already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'category' AND index_name = 'idx_category_updated_by');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================================
-- DATA VALIDATION AND CLEANUP
-- =============================================================================

-- Ensure item_type enum consistency
UPDATE item SET item_type = 'SIMPLE' WHERE item_type IS NULL OR item_type = '';

-- Set reasonable defaults for inventory fields where NULL
UPDATE item SET 
    safety_stock_default = CASE WHEN safety_stock_default IS NULL THEN 0 ELSE safety_stock_default END,
    reorder_point_default = CASE WHEN reorder_point_default IS NULL THEN 0 ELSE reorder_point_default END,
    reorder_quantity_default = CASE WHEN reorder_quantity_default IS NULL THEN 0 ELSE reorder_quantity_default END,
    is_serialized = CASE WHEN is_serialized IS NULL THEN FALSE ELSE is_serialized END,
    is_lot_tracked = CASE WHEN is_lot_tracked IS NULL THEN FALSE ELSE is_lot_tracked END;

-- =============================================================================
-- PERFORMANCE OPTIMIZATIONS
-- =============================================================================

-- Add composite indexes for common query patterns
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_tenant_status_type ON item(tenant_id, status, item_type);' ELSE 'SELECT ''Index idx_item_tenant_status_type already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_tenant_status_type');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_tenant_department ON item(tenant_id, department_id);' ELSE 'SELECT ''Index idx_item_tenant_department already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_tenant_department');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_tenant_brand ON item(tenant_id, brand_id);' ELSE 'SELECT ''Index idx_item_tenant_brand already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_tenant_brand');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE INDEX idx_item_tenant_category ON item(tenant_id, category_id);' ELSE 'SELECT ''Index idx_item_tenant_category already exists'';' END FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'item' AND index_name = 'idx_item_tenant_category');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Optimize table statistics for better query planning
ANALYZE TABLE item;
ANALYZE TABLE item_variant; 
ANALYZE TABLE category;
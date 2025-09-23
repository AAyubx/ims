-- V22__fix_duplicate_table_definitions.sql
-- Fix issues caused by duplicate table definitions in V14 and V16
-- Ensure data consistency and remove potential conflicts

-- =============================================================================
-- ENSURE MISSING TABLES EXIST
-- =============================================================================

-- Create uom_conversion table if it doesn't exist (in case V14 failed to create it)
SET @sql = (SELECT CASE WHEN COUNT(*) = 0 THEN 'CREATE TABLE uom_conversion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    from_uom_id BIGINT NOT NULL,
    to_uom_id BIGINT NOT NULL,
    conversion_factor DECIMAL(12,6) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_uom_conv_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_uom_conv_from FOREIGN KEY (from_uom_id) REFERENCES unit_of_measure(id) ON DELETE CASCADE,
    CONSTRAINT fk_uom_conv_to FOREIGN KEY (to_uom_id) REFERENCES unit_of_measure(id) ON DELETE CASCADE,
    CONSTRAINT fk_uom_conv_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_uom_conv_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    UNIQUE KEY uq_uom_conversion (from_uom_id, to_uom_id),
    CHECK (from_uom_id != to_uom_id),
    CHECK (conversion_factor > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;' ELSE 'SELECT ''Table uom_conversion already exists'';' END FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'uom_conversion');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================================================
-- RESOLVE DUPLICATE UOM DATA
-- =============================================================================

-- Clean up duplicate UoM entries that might have been created by both V14 and V16
-- Keep the first occurrence based on creation time
DELETE u1 FROM unit_of_measure u1
INNER JOIN unit_of_measure u2 
WHERE u1.id > u2.id 
  AND u1.tenant_id = u2.tenant_id 
  AND u1.code = u2.code;

-- =============================================================================
-- RESOLVE DUPLICATE ATTRIBUTE DEFINITION DATA  
-- =============================================================================

-- Clean up duplicate attribute definitions
DELETE a1 FROM attribute_definition a1
INNER JOIN attribute_definition a2
WHERE a1.id > a2.id
  AND a1.tenant_id = a2.tenant_id
  AND a1.code = a2.code;

-- =============================================================================
-- ENHANCE UOM DATA WITH MISSING ENTRIES FROM V14
-- =============================================================================

-- Add missing UoM entries that were in V14 but not in V16
INSERT IGNORE INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit, created_at) 
SELECT t.id, 'BOX', 'Box', 'Box packaging', 'COUNT', FALSE, NOW() 
FROM tenant t 
WHERE t.code = 'demo' 
  AND NOT EXISTS (SELECT 1 FROM unit_of_measure u WHERE u.tenant_id = t.id AND u.code = 'BOX');

INSERT IGNORE INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit, created_at) 
SELECT t.id, 'CASE', 'Case', 'Case packaging', 'COUNT', FALSE, NOW() 
FROM tenant t 
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM unit_of_measure u WHERE u.tenant_id = t.id AND u.code = 'CASE');

INSERT IGNORE INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit, created_at) 
SELECT t.id, 'G', 'Gram', 'Weight in grams', 'WEIGHT', FALSE, NOW() 
FROM tenant t 
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM unit_of_measure u WHERE u.tenant_id = t.id AND u.code = 'G');

INSERT IGNORE INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit, created_at) 
SELECT t.id, 'L', 'Liter', 'Volume in liters', 'VOLUME', TRUE, NOW() 
FROM tenant t 
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM unit_of_measure u WHERE u.tenant_id = t.id AND u.code = 'L');

INSERT IGNORE INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit, created_at) 
SELECT t.id, 'ML', 'Milliliter', 'Volume in milliliters', 'VOLUME', FALSE, NOW() 
FROM tenant t 
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM unit_of_measure u WHERE u.tenant_id = t.id AND u.code = 'ML');

-- =============================================================================
-- ADD MISSING UOM CONVERSIONS FROM V14
-- =============================================================================

-- Add UoM conversions that were in V14 but missing in V16
INSERT IGNORE INTO uom_conversion (tenant_id, from_uom_id, to_uom_id, conversion_factor, created_at)
SELECT t.id, kg.id, g.id, 1000.0, NOW()
FROM tenant t
JOIN unit_of_measure kg ON kg.tenant_id = t.id AND kg.code = 'KG'
JOIN unit_of_measure g ON g.tenant_id = t.id AND g.code = 'G'
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM uom_conversion uc WHERE uc.from_uom_id = kg.id AND uc.to_uom_id = g.id);

INSERT IGNORE INTO uom_conversion (tenant_id, from_uom_id, to_uom_id, conversion_factor, created_at)
SELECT t.id, g.id, kg.id, 0.001, NOW()
FROM tenant t
JOIN unit_of_measure g ON g.tenant_id = t.id AND g.code = 'G'
JOIN unit_of_measure kg ON kg.tenant_id = t.id AND kg.code = 'KG'
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM uom_conversion uc WHERE uc.from_uom_id = g.id AND uc.to_uom_id = kg.id);

INSERT IGNORE INTO uom_conversion (tenant_id, from_uom_id, to_uom_id, conversion_factor, created_at)
SELECT t.id, l.id, ml.id, 1000.0, NOW()
FROM tenant t
JOIN unit_of_measure l ON l.tenant_id = t.id AND l.code = 'L'
JOIN unit_of_measure ml ON ml.tenant_id = t.id AND ml.code = 'ML'
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM uom_conversion uc WHERE uc.from_uom_id = l.id AND uc.to_uom_id = ml.id);

INSERT IGNORE INTO uom_conversion (tenant_id, from_uom_id, to_uom_id, conversion_factor, created_at)
SELECT t.id, ml.id, l.id, 0.001, NOW()
FROM tenant t
JOIN unit_of_measure ml ON ml.tenant_id = t.id AND ml.code = 'ML'
JOIN unit_of_measure l ON l.tenant_id = t.id AND l.code = 'L'
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM uom_conversion uc WHERE uc.from_uom_id = ml.id AND uc.to_uom_id = l.id);

-- =============================================================================
-- ENHANCE ATTRIBUTE DEFINITIONS WITH V14 MISSING DATA
-- =============================================================================

-- Add missing attribute definitions that were in V14 but not in V16
INSERT IGNORE INTO attribute_definition (tenant_id, code, name, description, data_type, is_required, created_at) 
SELECT t.id, 'MATERIAL', 'Material', 'Item material composition', 'TEXT', FALSE, NOW() 
FROM tenant t 
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM attribute_definition a WHERE a.tenant_id = t.id AND a.code = 'MATERIAL');

INSERT IGNORE INTO attribute_definition (tenant_id, code, name, description, data_type, is_required, allowed_values, created_at) 
SELECT t.id, 'GENDER', 'Gender', 'Target gender for item', 'LIST', FALSE, JSON_ARRAY('Men', 'Women', 'Unisex', 'Kids'), NOW() 
FROM tenant t 
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM attribute_definition a WHERE a.tenant_id = t.id AND a.code = 'GENDER');

INSERT IGNORE INTO attribute_definition (tenant_id, code, name, description, data_type, is_required, created_at) 
SELECT t.id, 'WEIGHT', 'Weight', 'Item weight in grams', 'NUMBER', FALSE, NOW() 
FROM tenant t 
WHERE t.code = 'demo'
  AND NOT EXISTS (SELECT 1 FROM attribute_definition a WHERE a.tenant_id = t.id AND a.code = 'WEIGHT');

-- Update existing COLOR and SIZE attributes with enhanced values from V14
UPDATE attribute_definition 
SET allowed_values = JSON_ARRAY('Red', 'Blue', 'Green', 'Black', 'White', 'Yellow', 'Purple', 'Orange', 'Pink', 'Brown')
WHERE code = 'COLOR' 
  AND tenant_id IN (SELECT id FROM tenant WHERE code = 'demo')
  AND JSON_LENGTH(allowed_values) = 6; -- Only update if it has the limited set from V16

UPDATE attribute_definition 
SET allowed_values = JSON_ARRAY('XS', 'S', 'M', 'L', 'XL', 'XXL', '6', '7', '8', '9', '10', '11', '12')
WHERE code = 'SIZE' 
  AND tenant_id IN (SELECT id FROM tenant WHERE code = 'demo')
  AND JSON_LENGTH(allowed_values) = 6; -- Only update if it has the limited set from V16

-- =============================================================================
-- VERIFY DATA INTEGRITY
-- =============================================================================

-- Ensure all foreign key relationships are valid
UPDATE item SET department_id = NULL WHERE department_id IS NOT NULL 
  AND NOT EXISTS (SELECT 1 FROM department d WHERE d.id = item.department_id);

UPDATE item SET brand_id = NULL WHERE brand_id IS NOT NULL 
  AND NOT EXISTS (SELECT 1 FROM brand b WHERE b.id = item.brand_id);

UPDATE item SET base_uom_id = NULL WHERE base_uom_id IS NOT NULL 
  AND NOT EXISTS (SELECT 1 FROM unit_of_measure u WHERE u.id = item.base_uom_id);

UPDATE item SET sell_uom_id = NULL WHERE sell_uom_id IS NOT NULL 
  AND NOT EXISTS (SELECT 1 FROM unit_of_measure u WHERE u.id = item.sell_uom_id);

UPDATE item SET buy_uom_id = NULL WHERE buy_uom_id IS NOT NULL 
  AND NOT EXISTS (SELECT 1 FROM unit_of_measure u WHERE u.id = item.buy_uom_id);

-- =============================================================================
-- OPTIMIZATION
-- =============================================================================

-- Update table statistics after data changes
ANALYZE TABLE unit_of_measure;
ANALYZE TABLE uom_conversion;
ANALYZE TABLE attribute_definition;
ANALYZE TABLE item;
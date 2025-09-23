-- V17__add_missing_item_columns.sql
-- Add missing columns to item and item_variant tables for Hibernate validation

-- Add missing columns to item table using conditional statements
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='short_name') = 0, 'ALTER TABLE item ADD COLUMN short_name VARCHAR(128) AFTER name', 'SELECT ''Column short_name already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='description') = 0, 'ALTER TABLE item ADD COLUMN description TEXT AFTER short_name', 'SELECT ''Column description already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='department_id') = 0, 'ALTER TABLE item ADD COLUMN department_id BIGINT AFTER category_id', 'SELECT ''Column department_id already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='brand_id') = 0, 'ALTER TABLE item ADD COLUMN brand_id BIGINT AFTER department_id', 'SELECT ''Column brand_id already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='item_type') = 0, 'ALTER TABLE item ADD COLUMN item_type VARCHAR(32) NOT NULL DEFAULT ''SIMPLE'' AFTER brand_id', 'SELECT ''Column item_type already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='base_uom_id') = 0, 'ALTER TABLE item ADD COLUMN base_uom_id BIGINT AFTER item_type', 'SELECT ''Column base_uom_id already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='sell_uom_id') = 0, 'ALTER TABLE item ADD COLUMN sell_uom_id BIGINT AFTER base_uom_id', 'SELECT ''Column sell_uom_id already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='buy_uom_id') = 0, 'ALTER TABLE item ADD COLUMN buy_uom_id BIGINT AFTER sell_uom_id', 'SELECT ''Column buy_uom_id already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='average_cost') = 0, 'ALTER TABLE item ADD COLUMN average_cost DECIMAL(12,4) AFTER buy_uom_id', 'SELECT ''Column average_cost already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='created_by') = 0, 'ALTER TABLE item ADD COLUMN created_by BIGINT AFTER updated_at', 'SELECT ''Column created_by already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='updated_by') = 0, 'ALTER TABLE item ADD COLUMN updated_by BIGINT AFTER created_by', 'SELECT ''Column updated_by already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add missing columns to item_variant table using conditional statements
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item_variant' AND COLUMN_NAME='name') = 0, 'ALTER TABLE item_variant ADD COLUMN name VARCHAR(255) AFTER variant_sku', 'SELECT ''Column name already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item_variant' AND COLUMN_NAME='average_cost') = 0, 'ALTER TABLE item_variant ADD COLUMN average_cost DECIMAL(12,4) AFTER name', 'SELECT ''Column average_cost already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item_variant' AND COLUMN_NAME='created_by') = 0, 'ALTER TABLE item_variant ADD COLUMN created_by BIGINT AFTER updated_at', 'SELECT ''Column created_by already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item_variant' AND COLUMN_NAME='updated_by') = 0, 'ALTER TABLE item_variant ADD COLUMN updated_by BIGINT AFTER created_by', 'SELECT ''Column updated_by already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add missing columns to category table using conditional statements
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='category' AND COLUMN_NAME='department_id') = 0, 'ALTER TABLE category ADD COLUMN department_id BIGINT AFTER tenant_id', 'SELECT ''Column department_id already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='category' AND COLUMN_NAME='description') = 0, 'ALTER TABLE category ADD COLUMN description TEXT AFTER name', 'SELECT ''Column description already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='category' AND COLUMN_NAME='created_by') = 0, 'ALTER TABLE category ADD COLUMN created_by BIGINT AFTER updated_at', 'SELECT ''Column created_by already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='category' AND COLUMN_NAME='updated_by') = 0, 'ALTER TABLE category ADD COLUMN updated_by BIGINT AFTER created_by', 'SELECT ''Column updated_by already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
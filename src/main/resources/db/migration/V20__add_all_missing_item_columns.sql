-- V20__add_all_missing_item_columns.sql
-- Add all missing columns to item table to match the Item entity

-- Add tax_class column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='tax_class') = 0, 'ALTER TABLE item ADD COLUMN tax_class VARCHAR(32) AFTER buy_uom_id', 'SELECT ''Column tax_class already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add hs_code column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='hs_code') = 0, 'ALTER TABLE item ADD COLUMN hs_code VARCHAR(32) AFTER tax_class', 'SELECT ''Column hs_code already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add country_of_origin column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='country_of_origin') = 0, 'ALTER TABLE item ADD COLUMN country_of_origin VARCHAR(2) AFTER hs_code', 'SELECT ''Column country_of_origin already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add is_serialized column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='is_serialized') = 0, 'ALTER TABLE item ADD COLUMN is_serialized BOOLEAN NOT NULL DEFAULT FALSE AFTER country_of_origin', 'SELECT ''Column is_serialized already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add is_lot_tracked column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='is_lot_tracked') = 0, 'ALTER TABLE item ADD COLUMN is_lot_tracked BOOLEAN NOT NULL DEFAULT FALSE AFTER is_serialized', 'SELECT ''Column is_lot_tracked already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add shelf_life_days column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='shelf_life_days') = 0, 'ALTER TABLE item ADD COLUMN shelf_life_days INT AFTER is_lot_tracked', 'SELECT ''Column shelf_life_days already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add safety_stock_default column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='safety_stock_default') = 0, 'ALTER TABLE item ADD COLUMN safety_stock_default INT NOT NULL DEFAULT 0 AFTER shelf_life_days', 'SELECT ''Column safety_stock_default already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add reorder_point_default column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='reorder_point_default') = 0, 'ALTER TABLE item ADD COLUMN reorder_point_default INT NOT NULL DEFAULT 0 AFTER safety_stock_default', 'SELECT ''Column reorder_point_default already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add reorder_quantity_default column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='reorder_quantity_default') = 0, 'ALTER TABLE item ADD COLUMN reorder_quantity_default INT NOT NULL DEFAULT 0 AFTER reorder_point_default', 'SELECT ''Column reorder_quantity_default already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add standard_cost column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='standard_cost') = 0, 'ALTER TABLE item ADD COLUMN standard_cost DECIMAL(12,4) AFTER reorder_quantity_default', 'SELECT ''Column standard_cost already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add last_cost column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='last_cost') = 0, 'ALTER TABLE item ADD COLUMN last_cost DECIMAL(12,4) AFTER standard_cost', 'SELECT ''Column last_cost already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add meta_title column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='meta_title') = 0, 'ALTER TABLE item ADD COLUMN meta_title VARCHAR(255) AFTER base_price', 'SELECT ''Column meta_title already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add meta_description column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='meta_description') = 0, 'ALTER TABLE item ADD COLUMN meta_description TEXT AFTER meta_title', 'SELECT ''Column meta_description already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add search_keywords column
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='search_keywords') = 0, 'ALTER TABLE item ADD COLUMN search_keywords TEXT AFTER meta_description', 'SELECT ''Column search_keywords already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
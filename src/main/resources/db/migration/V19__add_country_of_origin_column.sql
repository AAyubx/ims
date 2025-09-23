-- V19__add_country_of_origin_column.sql
-- Add missing country_of_origin column to item table

-- Add country_of_origin column to item table if it doesn't exist
-- Fix: Don't reference hs_code column since it doesn't exist yet at this migration point
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='country_of_origin') = 0, 'ALTER TABLE item ADD COLUMN country_of_origin VARCHAR(2) AFTER base_price', 'SELECT ''Column country_of_origin already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
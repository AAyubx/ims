-- V18__add_base_price_column.sql
-- Add missing base_price column to item table

-- Add base_price column to item table if it doesn't exist
SET @stmt = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='item' AND COLUMN_NAME='base_price') = 0, 'ALTER TABLE item ADD COLUMN base_price DECIMAL(12,4) AFTER average_cost', 'SELECT ''Column base_price already exists'' AS message');
PREPARE stmt FROM @stmt; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- V25__add_missing_item_variant_columns.sql
-- Add all missing columns to item_variant table to match ItemVariant entity
-- This fixes the massive schema mismatch discovered during entity comparison

-- =============================================================================
-- ADD MISSING COLUMNS TO ITEM_VARIANT TABLE
-- =============================================================================

-- Add all missing columns that the ItemVariant entity expects
ALTER TABLE item_variant 
    ADD COLUMN short_name VARCHAR(128) AFTER name,
    ADD COLUMN upc VARCHAR(64) AFTER short_name,
    ADD COLUMN ean VARCHAR(64) AFTER upc,
    ADD COLUMN gtin VARCHAR(64) AFTER ean,
    ADD COLUMN base_uom_id BIGINT AFTER attributes_json,
    ADD COLUMN sell_uom_id BIGINT AFTER base_uom_id,
    ADD COLUMN buy_uom_id BIGINT AFTER sell_uom_id,
    ADD COLUMN weight_value DECIMAL(10,3) AFTER buy_uom_id,
    ADD COLUMN weight_uom_id BIGINT AFTER weight_value,
    ADD COLUMN volume_value DECIMAL(10,3) AFTER weight_uom_id,
    ADD COLUMN volume_uom_id BIGINT AFTER volume_value,
    ADD COLUMN length_value DECIMAL(10,3) AFTER volume_uom_id,
    ADD COLUMN width_value DECIMAL(10,3) AFTER length_value,
    ADD COLUMN height_value DECIMAL(10,3) AFTER width_value,
    ADD COLUMN dimension_uom_id BIGINT AFTER height_value,
    ADD COLUMN tax_class VARCHAR(32) AFTER dimension_uom_id,
    ADD COLUMN hs_code VARCHAR(32) AFTER tax_class,
    ADD COLUMN country_of_origin VARCHAR(2) AFTER hs_code,
    ADD COLUMN is_serialized BOOLEAN NOT NULL DEFAULT FALSE AFTER country_of_origin,
    ADD COLUMN is_lot_tracked BOOLEAN NOT NULL DEFAULT FALSE AFTER is_serialized,
    ADD COLUMN shelf_life_days INT AFTER is_lot_tracked,
    ADD COLUMN standard_cost DECIMAL(12,4) AFTER shelf_life_days,
    ADD COLUMN last_cost DECIMAL(12,4) AFTER standard_cost,
    ADD COLUMN base_price DECIMAL(12,4) AFTER average_cost,
    ADD COLUMN is_active_for_sale BOOLEAN NOT NULL DEFAULT TRUE AFTER base_price,
    ADD COLUMN is_active_for_purchase BOOLEAN NOT NULL DEFAULT TRUE AFTER is_active_for_sale;

-- Add foreign key constraints for UOM relationships
ALTER TABLE item_variant 
    ADD CONSTRAINT fk_variant_base_uom FOREIGN KEY (base_uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_variant_sell_uom FOREIGN KEY (sell_uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_variant_buy_uom FOREIGN KEY (buy_uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_variant_weight_uom FOREIGN KEY (weight_uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_variant_volume_uom FOREIGN KEY (volume_uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_variant_dimension_uom FOREIGN KEY (dimension_uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL;

-- Convert status column from ENUM to VARCHAR to match entity @Enumerated(EnumType.STRING)
ALTER TABLE item_variant MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Add indexes for performance
CREATE INDEX idx_variant_upc ON item_variant(upc);
CREATE INDEX idx_variant_ean ON item_variant(ean);
CREATE INDEX idx_variant_gtin ON item_variant(gtin);
CREATE INDEX idx_variant_status_active ON item_variant(status, is_active_for_sale, is_active_for_purchase);
CREATE INDEX idx_variant_base_uom ON item_variant(base_uom_id);
CREATE INDEX idx_variant_sell_uom ON item_variant(sell_uom_id);
CREATE INDEX idx_variant_buy_uom ON item_variant(buy_uom_id);

-- Update table statistics
ANALYZE TABLE item_variant;
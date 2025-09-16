-- V11: Enhance location table with geographical and hierarchy support
-- This migration adds geographical data, address information, and store hierarchy to the location table

-- Add new columns to location table for geographical and hierarchy support
ALTER TABLE location 
ADD COLUMN address_line1 VARCHAR(255) NULL COMMENT 'Primary address line',
ADD COLUMN address_line2 VARCHAR(255) NULL COMMENT 'Secondary address line (apartment, suite, etc.)',
ADD COLUMN city VARCHAR(100) NULL COMMENT 'City name',
ADD COLUMN state_province VARCHAR(100) NULL COMMENT 'State or Province',
ADD COLUMN postal_code VARCHAR(20) NULL COMMENT 'Postal/ZIP code',
ADD COLUMN country_code CHAR(2) NULL COMMENT 'ISO 3166-1 alpha-2 country code',
ADD COLUMN latitude DECIMAL(10,8) NULL COMMENT 'GPS latitude coordinate',
ADD COLUMN longitude DECIMAL(11,8) NULL COMMENT 'GPS longitude coordinate',
ADD COLUMN timezone VARCHAR(50) NULL COMMENT 'IANA timezone identifier (e.g., America/New_York)',
ADD COLUMN parent_location_id BIGINT NULL COMMENT 'Parent location for hierarchy (hub-and-spoke, regional management)',
ADD COLUMN store_manager_id BIGINT NULL COMMENT 'User ID of the store manager',
ADD COLUMN business_hours_json JSON NULL COMMENT 'Store operating hours by day of week',
ADD COLUMN capabilities_json JSON NULL COMMENT 'Store capabilities (pickup, delivery, returns, etc.)';

-- Add foreign key constraints for hierarchy and management
ALTER TABLE location
ADD CONSTRAINT fk_loc_parent FOREIGN KEY (parent_location_id) REFERENCES location(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_loc_manager FOREIGN KEY (store_manager_id) REFERENCES user_account(id) ON DELETE SET NULL;

-- Add indexes for performance
CREATE INDEX ix_location_country_city ON location(tenant_id, country_code, city);
CREATE INDEX ix_location_coordinates ON location(latitude, longitude) COMMENT 'For geographical proximity queries';
CREATE INDEX ix_location_parent ON location(parent_location_id) COMMENT 'For hierarchy traversal';
CREATE INDEX ix_location_manager ON location(store_manager_id) COMMENT 'For manager-based queries';
CREATE INDEX ix_location_timezone ON location(timezone) COMMENT 'For timezone-based operations';

-- Add comments to existing columns for clarity
ALTER TABLE location 
MODIFY COLUMN type VARCHAR(20) NOT NULL COMMENT 'Location type: STORE, WAREHOUSE, DISTRIBUTION_CENTER',
MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Location status: ACTIVE, INACTIVE, TEMPORARILY_CLOSED';
-- V13: Create location currency table for multi-currency support
-- This migration creates the location_currency table for managing currencies per location

-- Create location currency table
CREATE TABLE location_currency (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  location_id BIGINT NOT NULL,
  currency_code CHAR(3) NOT NULL COMMENT 'ISO 4217 currency code (USD, EUR, GBP, etc.)',
  is_primary BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether this is the primary currency for the location',
  exchange_rate DECIMAL(12,6) NOT NULL DEFAULT 1.0 COMMENT 'Exchange rate relative to tenant base currency',
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When exchange rate was last updated',
  
  -- Constraints
  UNIQUE KEY uq_location_currency (location_id, currency_code),
  KEY ix_location_currency_tenant (tenant_id),
  KEY ix_location_currency_primary (location_id, is_primary),
  
  -- Foreign keys
  CONSTRAINT fk_loc_currency_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_loc_currency_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE,
  
  -- Business rules
  CONSTRAINT chk_exchange_rate_positive CHECK (exchange_rate > 0),
  CONSTRAINT chk_currency_code_valid CHECK (LENGTH(currency_code) = 3 AND currency_code = UPPER(currency_code))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci 
COMMENT='Currency configurations per location for multi-currency operations';

-- Create store configuration table for flexible key-value configurations
CREATE TABLE store_configuration (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  location_id BIGINT NOT NULL,
  config_key VARCHAR(100) NOT NULL COMMENT 'Configuration key (e.g., max_capacity, operating_model)',
  config_value TEXT NULL COMMENT 'Configuration value (string, JSON, etc.)',
  config_type VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT 'Value type: STRING, JSON, BOOLEAN, NUMBER, DATE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  -- Constraints
  UNIQUE KEY uq_store_config (location_id, config_key),
  KEY ix_store_config_tenant (tenant_id),
  KEY ix_store_config_key (config_key),
  
  -- Foreign keys
  CONSTRAINT fk_store_config_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_store_config_location FOREIGN KEY (location_id) REFERENCES location(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci 
COMMENT='Flexible key-value configuration storage for stores';

-- Insert default currency configurations for demo tenant
-- These will be automatically created when a location is created
INSERT INTO location_currency (tenant_id, location_id, currency_code, is_primary, exchange_rate) 
SELECT 1, l.id, 'USD', TRUE, 1.0 
FROM location l 
WHERE l.tenant_id = 1 
AND NOT EXISTS (
    SELECT 1 FROM location_currency lc 
    WHERE lc.location_id = l.id AND lc.currency_code = 'USD'
);

-- Add some useful store configuration examples
INSERT INTO store_configuration (tenant_id, location_id, config_key, config_value, config_type) 
SELECT 1, l.id, 'max_capacity', '10000', 'NUMBER'
FROM location l 
WHERE l.tenant_id = 1;

INSERT INTO store_configuration (tenant_id, location_id, config_key, config_value, config_type) 
SELECT 1, l.id, 'supports_online_pickup', 'true', 'BOOLEAN'
FROM location l 
WHERE l.tenant_id = 1 AND l.type = 'STORE';
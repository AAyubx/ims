-- V12: Create tax jurisdiction table for multi-jurisdiction tax management
-- This migration creates the tax_jurisdiction table and links it to locations

-- Create tax jurisdiction table
CREATE TABLE tax_jurisdiction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  code VARCHAR(32) NOT NULL COMMENT 'Unique jurisdiction code (e.g., US-CA, UK-VAT, AU-GST)',
  name VARCHAR(255) NOT NULL COMMENT 'Human-readable jurisdiction name',
  country_code CHAR(2) NOT NULL COMMENT 'ISO 3166-1 alpha-2 country code',
  state_province VARCHAR(100) NULL COMMENT 'State or province if applicable',
  tax_rate DECIMAL(8,4) NOT NULL DEFAULT 0 COMMENT 'Tax rate as decimal (e.g., 0.0825 for 8.25%)',
  tax_type VARCHAR(20) NOT NULL COMMENT 'Type of tax: VAT, GST, SALES_TAX, INCOME_TAX, NONE',
  effective_date DATE NOT NULL COMMENT 'Date when this tax rate becomes effective',
  expiry_date DATE NULL COMMENT 'Date when this tax rate expires (NULL for indefinite)',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  -- Constraints
  UNIQUE KEY uq_tax_jurisdiction_code (tenant_id, code),
  KEY ix_tax_jurisdiction_country (tenant_id, country_code),
  KEY ix_tax_jurisdiction_effective (effective_date, expiry_date),
  
  -- Foreign keys
  CONSTRAINT fk_tax_jurisdiction_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  
  -- Business rules
  CONSTRAINT chk_tax_rate_valid CHECK (tax_rate >= 0 AND tax_rate <= 1),
  CONSTRAINT chk_effective_before_expiry CHECK (expiry_date IS NULL OR effective_date <= expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci 
COMMENT='Tax jurisdiction definitions for multi-region operations';

-- Link locations to tax jurisdictions
ALTER TABLE location 
ADD COLUMN tax_jurisdiction_id BIGINT NULL COMMENT 'Tax jurisdiction applicable to this location',
ADD CONSTRAINT fk_loc_tax_jurisdiction FOREIGN KEY (tax_jurisdiction_id) REFERENCES tax_jurisdiction(id) ON DELETE SET NULL;

-- Add index for location tax jurisdiction lookups
CREATE INDEX ix_location_tax_jurisdiction ON location(tax_jurisdiction_id);

-- Insert some common tax jurisdiction templates (can be customized per tenant)
INSERT INTO tax_jurisdiction (tenant_id, code, name, country_code, state_province, tax_rate, tax_type, effective_date) VALUES
(1, 'US-FEDERAL', 'United States Federal Tax', 'US', NULL, 0.0000, 'NONE', '2024-01-01'),
(1, 'US-CA', 'California State Tax', 'US', 'California', 0.0725, 'SALES_TAX', '2024-01-01'),
(1, 'US-NY', 'New York State Tax', 'US', 'New York', 0.0800, 'SALES_TAX', '2024-01-01'),
(1, 'US-TX', 'Texas State Tax', 'US', 'Texas', 0.0625, 'SALES_TAX', '2024-01-01'),
(1, 'UK-VAT', 'United Kingdom VAT', 'GB', NULL, 0.2000, 'VAT', '2024-01-01'),
(1, 'AU-GST', 'Australia GST', 'AU', NULL, 0.1000, 'GST', '2024-01-01'),
(1, 'CA-GST', 'Canada GST', 'CA', NULL, 0.0500, 'GST', '2024-01-01'),
(1, 'DE-VAT', 'Germany VAT', 'DE', NULL, 0.1900, 'VAT', '2024-01-01'),
(1, 'FR-VAT', 'France VAT', 'FR', NULL, 0.2000, 'VAT', '2024-01-01');
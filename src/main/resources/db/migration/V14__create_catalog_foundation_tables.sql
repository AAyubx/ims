-- V14__create_catalog_foundation_tables.sql
-- Item Master & Catalog Management Foundation
-- Generated: 2025-09-23

-- Department table (top-level retail division)
CREATE TABLE department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    tax_class_default VARCHAR(32),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_dept_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_dept_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_dept_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    UNIQUE KEY uq_dept_code_tenant (tenant_id, code),
    INDEX idx_dept_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Brand table (orthogonal to category tree)
CREATE TABLE brand (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    logo_url VARCHAR(512),
    vendor_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_brand_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_brand_vendor FOREIGN KEY (vendor_id) REFERENCES supplier(id) ON DELETE SET NULL,
    CONSTRAINT fk_brand_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_brand_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    UNIQUE KEY uq_brand_code_tenant (tenant_id, code),
    INDEX idx_brand_tenant_status (tenant_id, status),
    INDEX idx_brand_vendor (vendor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Attribute definitions for items
CREATE TABLE attribute_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    data_type VARCHAR(20) NOT NULL, -- TEXT, NUMBER, BOOLEAN, LIST, DATE
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    allowed_values JSON, -- For LIST type attributes
    validation_rules JSON, -- Additional validation rules
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_attr_def_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_attr_def_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_attr_def_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    UNIQUE KEY uq_attr_code_tenant (tenant_id, code),
    INDEX idx_attr_tenant_type (tenant_id, data_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Attribute sets - which attributes belong to which categories
CREATE TABLE attribute_set (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    attribute_definition_id BIGINT NOT NULL,
    is_required_for_variants BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    CONSTRAINT fk_attr_set_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_attr_set_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE,
    CONSTRAINT fk_attr_set_def FOREIGN KEY (attribute_definition_id) REFERENCES attribute_definition(id) ON DELETE CASCADE,
    CONSTRAINT fk_attr_set_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    UNIQUE KEY uq_attr_set (category_id, attribute_definition_id),
    INDEX idx_attr_set_tenant (tenant_id),
    INDEX idx_attr_set_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Units of Measure
CREATE TABLE unit_of_measure (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(64) NOT NULL,
    description TEXT,
    unit_type VARCHAR(32) NOT NULL, -- WEIGHT, VOLUME, LENGTH, COUNT, AREA, TIME
    is_base_unit BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_uom_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_uom_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_uom_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    UNIQUE KEY uq_uom_code_tenant (tenant_id, code),
    INDEX idx_uom_tenant_type (tenant_id, unit_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- UoM Conversions
CREATE TABLE uom_conversion (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Item attribute values (for both parent items and variants)
CREATE TABLE item_attribute_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    attribute_definition_id BIGINT NOT NULL,
    value TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_item_attr_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_attr_item FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_attr_def FOREIGN KEY (attribute_definition_id) REFERENCES attribute_definition(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_attr_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_item_attr_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    UNIQUE KEY uq_item_attr (item_id, attribute_definition_id),
    INDEX idx_item_attr_tenant (tenant_id),
    INDEX idx_item_attr_def (attribute_definition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Barcodes (multiple per variant)
CREATE TABLE item_barcode (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    barcode VARCHAR(64) NOT NULL,
    barcode_type VARCHAR(20) NOT NULL DEFAULT 'EAN13', -- UPC, EAN13, EAN8, CODE128, etc.
    uom_id BIGINT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_barcode_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_barcode_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE CASCADE,
    CONSTRAINT fk_barcode_uom FOREIGN KEY (uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL,
    CONSTRAINT fk_barcode_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_barcode_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    UNIQUE KEY uq_barcode_global (tenant_id, barcode),
    INDEX idx_barcode_variant (variant_id),
    INDEX idx_barcode_type (barcode_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Item media (images, documents, etc.)
CREATE TABLE item_media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    media_type VARCHAR(20) NOT NULL, -- IMAGE, DOCUMENT, VIDEO, AUDIO
    url VARCHAR(1024) NOT NULL,
    alt_text VARCHAR(255),
    description TEXT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    file_size BIGINT,
    mime_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_media_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_item FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_media_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    INDEX idx_media_tenant (tenant_id),
    INDEX idx_media_item (item_id),
    INDEX idx_media_type (media_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Supplier item relationships
CREATE TABLE supplier_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    supplier_item_code VARCHAR(64),
    moq INT NOT NULL DEFAULT 1,
    lead_time_days INT NOT NULL DEFAULT 0,
    unit_cost DECIMAL(12,4),
    currency_code CHAR(3) DEFAULT 'USD',
    is_preferred BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_supp_item_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_supp_item_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id) ON DELETE CASCADE,
    CONSTRAINT fk_supp_item_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE CASCADE,
    CONSTRAINT fk_supp_item_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_supp_item_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    UNIQUE KEY uq_supplier_item (supplier_id, variant_id),
    INDEX idx_supp_item_tenant (tenant_id),
    INDEX idx_supp_item_variant (variant_id),
    INDEX idx_supp_item_preferred (is_preferred)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insert default UoM data
INSERT INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit) 
SELECT t.id, 'EA', 'Each', 'Individual unit', 'COUNT', TRUE FROM tenant t WHERE t.code = 'demo';

INSERT INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit) 
SELECT t.id, 'BOX', 'Box', 'Box packaging', 'COUNT', FALSE FROM tenant t WHERE t.code = 'demo';

INSERT INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit) 
SELECT t.id, 'CASE', 'Case', 'Case packaging', 'COUNT', FALSE FROM tenant t WHERE t.code = 'demo';

INSERT INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit) 
SELECT t.id, 'KG', 'Kilogram', 'Weight in kilograms', 'WEIGHT', TRUE FROM tenant t WHERE t.code = 'demo';

INSERT INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit) 
SELECT t.id, 'G', 'Gram', 'Weight in grams', 'WEIGHT', FALSE FROM tenant t WHERE t.code = 'demo';

INSERT INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit) 
SELECT t.id, 'L', 'Liter', 'Volume in liters', 'VOLUME', TRUE FROM tenant t WHERE t.code = 'demo';

INSERT INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit) 
SELECT t.id, 'ML', 'Milliliter', 'Volume in milliliters', 'VOLUME', FALSE FROM tenant t WHERE t.code = 'demo';

-- Insert default UoM conversions
INSERT INTO uom_conversion (tenant_id, from_uom_id, to_uom_id, conversion_factor)
SELECT t.id, kg.id, g.id, 1000.0
FROM tenant t
JOIN unit_of_measure kg ON kg.tenant_id = t.id AND kg.code = 'KG'
JOIN unit_of_measure g ON g.tenant_id = t.id AND g.code = 'G'
WHERE t.code = 'demo';

INSERT INTO uom_conversion (tenant_id, from_uom_id, to_uom_id, conversion_factor)
SELECT t.id, g.id, kg.id, 0.001
FROM tenant t
JOIN unit_of_measure g ON g.tenant_id = t.id AND g.code = 'G'
JOIN unit_of_measure kg ON kg.tenant_id = t.id AND kg.code = 'KG'
WHERE t.code = 'demo';

INSERT INTO uom_conversion (tenant_id, from_uom_id, to_uom_id, conversion_factor)
SELECT t.id, l.id, ml.id, 1000.0
FROM tenant t
JOIN unit_of_measure l ON l.tenant_id = t.id AND l.code = 'L'
JOIN unit_of_measure ml ON ml.tenant_id = t.id AND ml.code = 'ML'
WHERE t.code = 'demo';

INSERT INTO uom_conversion (tenant_id, from_uom_id, to_uom_id, conversion_factor)
SELECT t.id, ml.id, l.id, 0.001
FROM tenant t
JOIN unit_of_measure ml ON ml.tenant_id = t.id AND ml.code = 'ML'
JOIN unit_of_measure l ON l.tenant_id = t.id AND l.code = 'L'
WHERE t.code = 'demo';

-- Sample default attribute definitions
INSERT INTO attribute_definition (tenant_id, code, name, description, data_type, is_required, allowed_values) 
SELECT t.id, 'COLOR', 'Color', 'Item color attribute', 'LIST', FALSE, JSON_ARRAY('Red', 'Blue', 'Green', 'Black', 'White', 'Yellow', 'Purple', 'Orange', 'Pink', 'Brown') 
FROM tenant t WHERE t.code = 'demo';

INSERT INTO attribute_definition (tenant_id, code, name, description, data_type, is_required, allowed_values) 
SELECT t.id, 'SIZE', 'Size', 'Item size attribute', 'LIST', FALSE, JSON_ARRAY('XS', 'S', 'M', 'L', 'XL', 'XXL', '6', '7', '8', '9', '10', '11', '12') 
FROM tenant t WHERE t.code = 'demo';

INSERT INTO attribute_definition (tenant_id, code, name, description, data_type, is_required) 
SELECT t.id, 'MATERIAL', 'Material', 'Item material composition', 'TEXT', FALSE 
FROM tenant t WHERE t.code = 'demo';

INSERT INTO attribute_definition (tenant_id, code, name, description, data_type, is_required, allowed_values) 
SELECT t.id, 'GENDER', 'Gender', 'Target gender for item', 'LIST', FALSE, JSON_ARRAY('Men', 'Women', 'Unisex', 'Kids') 
FROM tenant t WHERE t.code = 'demo';

INSERT INTO attribute_definition (tenant_id, code, name, description, data_type, is_required) 
SELECT t.id, 'WEIGHT', 'Weight', 'Item weight in grams', 'NUMBER', FALSE 
FROM tenant t WHERE t.code = 'demo';
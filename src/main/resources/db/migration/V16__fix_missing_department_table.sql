-- V16__fix_missing_department_table.sql
-- Fix missing department table from V14 migration issues

-- Create department table if it doesn't exist
CREATE TABLE IF NOT EXISTS department (
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

-- Create brand table if it doesn't exist  
CREATE TABLE IF NOT EXISTS brand (
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

-- Create other missing tables from V14 if they don't exist
CREATE TABLE IF NOT EXISTS attribute_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    data_type VARCHAR(20) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    allowed_values JSON,
    validation_rules JSON,
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

-- Create attribute_set table if it doesn't exist
CREATE TABLE IF NOT EXISTS attribute_set (
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

-- Create unit_of_measure table if it doesn't exist
CREATE TABLE IF NOT EXISTS unit_of_measure (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(10) NOT NULL,
    name VARCHAR(64) NOT NULL,
    description TEXT,
    unit_type VARCHAR(32) NOT NULL,
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

-- Insert default UoM data if not exists
INSERT IGNORE INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit) 
SELECT t.id, 'EA', 'Each', 'Individual unit', 'COUNT', TRUE FROM tenant t WHERE t.code = 'demo';

INSERT IGNORE INTO unit_of_measure (tenant_id, code, name, description, unit_type, is_base_unit) 
SELECT t.id, 'KG', 'Kilogram', 'Weight in kilograms', 'WEIGHT', TRUE FROM tenant t WHERE t.code = 'demo';

-- Insert default attribute definitions if not exists
INSERT IGNORE INTO attribute_definition (tenant_id, code, name, description, data_type, is_required, allowed_values) 
SELECT t.id, 'COLOR', 'Color', 'Item color attribute', 'LIST', FALSE, JSON_ARRAY('Red', 'Blue', 'Green', 'Black', 'White', 'Yellow') 
FROM tenant t WHERE t.code = 'demo';

INSERT IGNORE INTO attribute_definition (tenant_id, code, name, description, data_type, is_required, allowed_values) 
SELECT t.id, 'SIZE', 'Size', 'Item size attribute', 'LIST', FALSE, JSON_ARRAY('XS', 'S', 'M', 'L', 'XL', 'XXL') 
FROM tenant t WHERE t.code = 'demo';
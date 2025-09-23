-- V24__create_missing_item_attribute_value_table.sql
-- Create the missing item_attribute_value table that should have been created in V14
-- This table is required by the ItemAttributeValue entity

-- =============================================================================
-- CREATE MISSING ITEM_ATTRIBUTE_VALUE TABLE
-- =============================================================================

-- Create item_attribute_value table (missing from V14 execution)
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
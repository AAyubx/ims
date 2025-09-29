-- V29__create_gs1_configuration_table.sql
-- Create GS1 GTIN allocation management table

CREATE TABLE gs1_configuration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    gs1_prefix VARCHAR(10) NOT NULL COMMENT 'GS1 Company Prefix assigned to tenant',
    prefix_capacity INT NOT NULL COMMENT 'Maximum number of GTINs available',
    next_sequence BIGINT NOT NULL DEFAULT 1 COMMENT 'Next sequence number for GTIN allocation',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_gs1_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_gs1_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE RESTRICT,
    CONSTRAINT fk_gs1_updated_by FOREIGN KEY (updated_by) REFERENCES user_account(id) ON DELETE SET NULL,
    
    UNIQUE INDEX uq_gs1_tenant_prefix (tenant_id, gs1_prefix),
    INDEX idx_gs1_tenant (tenant_id),
    INDEX idx_gs1_active (tenant_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
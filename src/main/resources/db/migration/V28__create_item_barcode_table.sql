-- V28__create_item_barcode_table.sql
-- Create barcode management table following project conventions

CREATE TABLE item_barcode (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    barcode VARCHAR(64) NOT NULL,
    barcode_type VARCHAR(32) NOT NULL COMMENT 'UPC_A, UPC_E, EAN_13, EAN_8, ITF_14, GS1_128, CODE_128, CODE_39, DATAMATRIX_GS1, QR_GS1_LINK',
    uom_id BIGINT NULL,
    pack_level VARCHAR(16) NULL COMMENT 'EACH, INNER, CASE, PALLET',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(16) NOT NULL DEFAULT 'RESERVED' COMMENT 'RESERVED, ACTIVE, DEPRECATED, BLOCKED',
    ai_payload JSON NULL COMMENT 'Parsed GS1 Application Identifiers',
    label_template_id BIGINT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_barcode_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
    CONSTRAINT fk_barcode_variant FOREIGN KEY (variant_id) REFERENCES item_variant(id) ON DELETE CASCADE,
    CONSTRAINT fk_barcode_uom FOREIGN KEY (uom_id) REFERENCES unit_of_measure(id) ON DELETE SET NULL,
    CONSTRAINT fk_barcode_created_by FOREIGN KEY (created_by) REFERENCES user_account(id) ON DELETE RESTRICT,
    
    UNIQUE INDEX uq_barcode_tenant (tenant_id, barcode),
    INDEX idx_barcode_variant (tenant_id, variant_id),
    INDEX idx_barcode_primary (tenant_id, is_primary),
    INDEX idx_barcode_lookup (tenant_id, barcode),
    INDEX idx_barcode_status (tenant_id, status),
    INDEX idx_barcode_type (tenant_id, barcode_type),
    INDEX idx_barcode_pack_level (tenant_id, pack_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
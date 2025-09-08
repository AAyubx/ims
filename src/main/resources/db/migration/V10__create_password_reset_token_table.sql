-- Create password reset token table
-- This table stores tokens for password reset functionality

-- Drop table if it exists (to handle failed migration attempts)
DROP TABLE IF EXISTS password_reset_token;

CREATE TABLE password_reset_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(320) NOT NULL,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    ip_address VARCHAR(45) NULL COMMENT 'IP address where reset was requested (supports IPv6)',
    user_agent VARCHAR(500) NULL COMMENT 'Browser user agent string',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    
    -- Foreign key constraints
    CONSTRAINT fk_password_reset_token_tenant 
        FOREIGN KEY (tenant_id) REFERENCES tenant(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_password_reset_token_user 
        FOREIGN KEY (user_id) REFERENCES user_account(id) 
        ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_password_reset_token (token),
    INDEX idx_password_reset_user_email (email),
    INDEX idx_password_reset_expires_at (expires_at),
    INDEX idx_password_reset_user_id (user_id),
    INDEX idx_password_reset_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
COMMENT='Password reset tokens with expiry and usage tracking';

-- Insert configuration entries
INSERT INTO system_config (config_key, config_value, description)
VALUES 
('password_reset_token_expiry_hours', '24', 'Hours after which password reset tokens expire'),
('password_reset_rate_limit_per_hour', '3', 'Maximum password reset requests per email per hour')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description);
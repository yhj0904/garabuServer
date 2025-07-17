-- Create table for Expo push tokens
CREATE TABLE IF NOT EXISTS expo_user_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    expo_token VARCHAR(200) NOT NULL UNIQUE,
    registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    platform VARCHAR(20),
    app_version VARCHAR(20),
    
    INDEX idx_user_device (user_id, device_id),
    INDEX idx_expo_token (expo_token),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment
ALTER TABLE expo_user_token COMMENT = 'Expo push notification tokens for users';
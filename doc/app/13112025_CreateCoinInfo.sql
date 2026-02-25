CREATE TABLE coin_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coin_id BIGINT NOT NULL,
    coingecko_id VARCHAR(255) DEFAULT NULL,
    total_supply DECIMAL(36, 18) DEFAULT 0,
    max_supply DECIMAL(36, 18) DEFAULT 0,
    circulating_supply DECIMAL(36, 18) DEFAULT 0 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

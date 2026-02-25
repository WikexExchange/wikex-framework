CREATE TABLE IF NOT EXISTS member_funding_wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    coin_id VARCHAR(64) NOT NULL,
    balance DECIMAL(32,16) NOT NULL DEFAULT 0,
    frozen_balance DECIMAL(32,16) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_member_coin (member_id, coin_id),
    KEY idx_member_id (member_id),
    KEY idx_coin_id (coin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

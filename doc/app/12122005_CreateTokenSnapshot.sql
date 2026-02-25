CREATE TABLE token_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    token_symbol VARCHAR(50) NOT NULL,
    snapshot_date DATE NOT NULL,
    snapshot_quantity DECIMAL(36,18) NOT NULL DEFAULT 0,
    snapshot_price DECIMAL(36,18) NOT NULL DEFAULT 0,
    snapshot_value DECIMAL(36,18) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_member_token_date (member_id, token_symbol, snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
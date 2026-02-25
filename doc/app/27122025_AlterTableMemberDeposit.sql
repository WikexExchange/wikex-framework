ALTER TABLE member_deposit
ADD COLUMN asset_symbol VARCHAR(50),
ADD COLUMN status TINYINT(1) NOT NULL DEFAULT 0,
ADD COLUMN confirmations INT NOT NULL DEFAULT 0,
ADD COLUMN from_address VARCHAR(255),
ADD COLUMN asset_contract VARCHAR(255),
ADD COLUMN blockchain VARCHAR(50),
ADD COLUMN chain_key VARCHAR(50),
ADD COLUMN amount_raw VARCHAR(100),
ADD COLUMN decimals INT;

ALTER TABLE member_deposit
ADD UNIQUE KEY uk_tx_log (txid, log_index);
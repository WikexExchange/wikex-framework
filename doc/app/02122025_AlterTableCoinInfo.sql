ALTER TABLE coin_info
ADD COLUMN market_cap_usd DECIMAL(36, 18) DEFAULT NULL AFTER circulating_supply,
ADD COLUMN fdv_usd DECIMAL(36, 18) DEFAULT NULL AFTER market_cap_usd,
ADD COLUMN description TEXT DEFAULT NULL AFTER fdv_usd;
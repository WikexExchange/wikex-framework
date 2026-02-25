CREATE TABLE mystery_box (
    id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    code VARCHAR(10) UNIQUE,
    type TINYINT(4) DEFAULT 0 COMMENT '1=USDT, 2=NFT, 3=VOUCHER',
    is_active BIT(1),
    member_id BIGINT(20) DEFAULT 0,
    member_active_at DATETIME,
    value VARCHAR(10),
    claim_reward_open_at DATETIME,
    extra_data TEXT
);

CREATE TABLE reward_period_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type TINYINT NOT NULL COMMENT 'Reward type: 0=trading, 2=inviter, 3=commission, 4=pnl',
    period VARCHAR(10) NOT NULL COMMENT 'Period: week | month',
    period_no INT NOT NULL COMMENT 'Week no or month no',
    year_no INT NOT NULL COMMENT 'Year no'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Reward Period type';
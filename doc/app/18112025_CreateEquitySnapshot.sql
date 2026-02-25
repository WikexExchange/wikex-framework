CREATE TABLE equity_snapshot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    `date` DATE NOT NULL,
    total_equity DECIMAL(18,8) NOT NULL,
    total_pnl DECIMAL(18,8) NOT NULL,
    realized_pnl DECIMAL(18,8) NOT NULL,
    unrealized_pnl DECIMAL(18,8) NOT NULL,
    UNIQUE KEY uk_member_date (member_id, `date`)
);
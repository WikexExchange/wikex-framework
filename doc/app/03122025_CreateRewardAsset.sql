CREATE TABLE reward_asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period VARCHAR(50) NOT NULL,
    period_no INT NOT NULL,
    type INT NOT NULL,
    rank INT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    UNIQUE KEY uq_reward_asset (period, type, rank)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
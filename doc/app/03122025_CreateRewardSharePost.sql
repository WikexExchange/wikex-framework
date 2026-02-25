CREATE TABLE `reward_share_post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT ,
    `member_id` BIGINT NOT NULL,
    `type` TINYINT NOT NULL COMMENT 'Reward type: 0=trading, 2=inviter, 3=commission, 4=pnl',
    `period` VARCHAR(10) NOT NULL COMMENT 'Period: week | month',
    `period_no` INT NOT NULL COMMENT 'Week number or month number',
    `post_url` VARCHAR(500) DEFAULT NULL COMMENT 'User post URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0 = pending, 1 = awaiting_approval, 2 = verified',
    `create_time` DATETIME NOT NULL COMMENT 'Created time',
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Reward Share Post';
package com.wikex.wikex.user.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CampaignRewardHubVO {
    private String address;
    private BigDecimal totalReward;
    private Integer level;
    private Integer currentLevel;
    private Integer currentPoint;
    private Integer nextPoint;
}

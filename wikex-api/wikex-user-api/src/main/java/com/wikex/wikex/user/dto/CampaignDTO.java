package com.wikex.wikex.user.dto;

import lombok.Data;

@Data
public class CampaignDTO {
    private CampaignTradeConfigDTO trading;
    private CampaignTradeConfigDTO friendTrading;
    private CampaignTradeConfigDTO inviter;
}

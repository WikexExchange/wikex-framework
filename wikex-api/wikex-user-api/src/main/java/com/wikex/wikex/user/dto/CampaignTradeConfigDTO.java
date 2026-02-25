package com.wikex.wikex.user.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CampaignTradeConfigDTO {
    private BigDecimal totalVolumn;
    private List<TradeConfig> tradeProcess;

    @Data
    public static class TradeConfig {
        private String id;
        private BigDecimal volumn;
        private BigDecimal value;
        private BigDecimal earn;
        private BigDecimal claimed;
    }
}

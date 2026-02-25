package com.wikex.wikex.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class CampaignSettingExtraDataDTO {
    private List<Integer> ignore_numbers;
    private List<TradeConfig> tradeConfigs;
    private List<TradeConfig> friendTradeConfigs;
    private List<TradeConfig> inviterConfigs;
    private List<LevelPoint> levelConfigs;

    @Data
    public static class TradeConfig {
        private Integer key;
        private Integer value;
    }

    @Data
    public static class LevelPoint {
        private Integer from;
        private Integer to;
        private Integer level;
    }
}

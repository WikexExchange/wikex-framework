package com.wikex.wikex.user.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CampaignLeadboardVO {
    private List<obj> process;
    private List<leadboard> leadboards;

    @Data
    public static class obj {
        private String name;
        private Integer index;
        private boolean active;
    }

    @Data
    public static class leadboard {
        private Integer rank;
        private String username;
        private String rewardWallet;
        private Long uid;
        private BigDecimal total;
    }
}

package com.wikex.wikex.user.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class CampaignClaimRewardVO {
    private BigDecimal total;
    private BigDecimal myBoxUsdt;
    private Integer myBoxNft;
    private List<history> histories;

    @Data
    public static class history {
        private String type;
        private String desc;
        private BigDecimal amount;
        private String status;
        private Date time;
    }
}

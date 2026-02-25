package com.wikex.wikex.user.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LeadboardScoreVO {
    private Integer year;
    private Integer week;
    private Integer month;
    private String timeRange;
    private List<leadboard> leadboards;

    @Data
    public static class leadboard {
        private Integer rank;
        private String username;
        private Long uid;
        private BigDecimal pnl;
        private BigDecimal referral;
        private BigDecimal volumn;
        private BigDecimal commision;
        private BigDecimal point;
    }
}
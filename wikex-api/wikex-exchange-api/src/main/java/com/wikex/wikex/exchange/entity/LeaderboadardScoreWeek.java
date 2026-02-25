package com.wikex.wikex.exchange.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "leaderboard_score_weeks")
public class LeaderboadardScoreWeek {
    private Long rank;
    private Integer memberId;
    private Integer monthNo;
    private Integer yearNo;
    private Integer weekNo;
    private BigDecimal pnl;
    private BigDecimal referral;
    private BigDecimal volumn;
    private BigDecimal commision;
    private BigDecimal point;
}
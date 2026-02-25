package com.wikex.wikex.exchange.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "leaderboard_scores")
public class LeaderboardScore {
    private Long rank;
    private Integer memberId;
    private BigDecimal pnl;
    private BigDecimal referral;
    private BigDecimal volumn;
    private BigDecimal commision;
    private BigDecimal point;
}
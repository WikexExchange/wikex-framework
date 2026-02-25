package com.wikex.wikex.exchange.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "trading_volumn_bonus")
public class TradingVolumnBonus {
    @Indexed
    private String _id;
    private String campaignId;
    private Integer memberId;
    private Integer type;
    private BigDecimal volumn;
    private BigDecimal bonus;
    private BigDecimal claimed;
}

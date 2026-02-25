package com.wikex.wikex.exchange.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "trading_volumns")
public class TradingVolumn {
    private String campaignId;
    private Integer memberId;
    private Integer type;
    private BigDecimal volumn;
}

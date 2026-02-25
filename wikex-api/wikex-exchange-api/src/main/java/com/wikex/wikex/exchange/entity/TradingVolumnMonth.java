package com.wikex.wikex.exchange.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "trading_volumn_months")
public class TradingVolumnMonth {
    private String campaignId;
    private Integer memberId;
    private Integer monthNo;
    private Integer yearNo;
    private Integer type;
    private BigDecimal volumn;
}

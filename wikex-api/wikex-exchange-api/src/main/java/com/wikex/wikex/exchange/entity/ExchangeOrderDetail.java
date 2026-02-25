package com.wikex.wikex.exchange.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "exchange_order_detail")
public class ExchangeOrderDetail {
    @Indexed
    private String orderId;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal turnover;
    private BigDecimal fee;
    private long time;
}

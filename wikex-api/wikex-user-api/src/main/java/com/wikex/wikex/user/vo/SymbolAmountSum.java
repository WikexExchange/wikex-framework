package com.wikex.wikex.user.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for aggregated transaction amounts
 * Used by SQL SUM + GROUP BY queries
 */
@Data
public class SymbolAmountSum {
    /**
     * Coin symbol (e.g., BTC, ETH, USDT)
     */
    private String symbol;

    /**
     * Transaction type code
     */
    private Integer type;

    /**
     * Total amount (summed by database)
     */
    private BigDecimal totalAmount;
}

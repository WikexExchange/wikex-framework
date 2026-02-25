package com.wikex.wikex.user.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EquityCalculateDTO {
    private BigDecimal totalEquity;
    private BigDecimal totalOpenValue;
    private BigDecimal totalPnl;

    public EquityCalculateDTO(BigDecimal totalEquity, BigDecimal totalOpenValue, BigDecimal totalPnl) {
        this.totalEquity = totalEquity;
        this.totalOpenValue = totalOpenValue;
        this.totalPnl = totalPnl;
    }
}

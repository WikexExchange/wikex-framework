package com.wikex.wikex.kline.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KLine {
    public KLine(){

    }
    public KLine(String period){
        this.period = period;
    }
    private BigDecimal openPrice = BigDecimal.ZERO;
    private BigDecimal highestPrice  = BigDecimal.ZERO;
    private BigDecimal lowestPrice  = BigDecimal.ZERO;
    private BigDecimal closePrice  = BigDecimal.ZERO;
    private long time;
    private String period;


    private int count;

    private BigDecimal volume = BigDecimal.ZERO;

    private BigDecimal turnover = BigDecimal.ZERO;
}

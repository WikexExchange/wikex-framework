package com.wikex.wikex.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class KLineTemp implements Serializable {
    private static final long serialVersionUID = 1L;
    public KLineTemp(){

    }
    public KLineTemp(String period){
        this.period = period;
    }
    private BigDecimal openPrice = BigDecimal.ZERO;
    private BigDecimal highestPrice  = BigDecimal.ZERO;
    private BigDecimal lowestPrice  = BigDecimal.ZERO;
    private BigDecimal closePrice  = BigDecimal.ZERO;
    private long time;
    private String period;
    private long lastSyncTime;
    
    private int count;
    
    private BigDecimal volume = BigDecimal.ZERO;
    
    private BigDecimal turnover = BigDecimal.ZERO;
}

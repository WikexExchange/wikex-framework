package com.wikex.wikex.pojo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoinThumb {
    private String symbol;
    private BigDecimal open = BigDecimal.ZERO;
    private BigDecimal high = BigDecimal.ZERO;
    private BigDecimal low = BigDecimal.ZERO;
    private BigDecimal close = BigDecimal.ZERO;
    private BigDecimal chg = BigDecimal.ZERO.setScale(2);
    private BigDecimal change = BigDecimal.ZERO.setScale(2);
    private BigDecimal volume = BigDecimal.ZERO.setScale(2);
    private BigDecimal turnover = BigDecimal.ZERO;
    private BigDecimal feePercent = BigDecimal.ZERO;

    private String coinUrl;
    private String coinName;
    private Integer coinScale;
    private Integer baseCoinScale;
    private BigDecimal lastDayClose = BigDecimal.ZERO;

    private BigDecimal usdRate;

    private BigDecimal baseUsdRate;
    private Integer robotType;

    private int zone;
}

package com.wikex.wikex.option.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.io.Serializable;

import com.wikex.wikex.constant.BooleanEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class OptionCoin implements Serializable {

    private static final long serialVersionUID = 1L;

    
    private String symbol;

    
    private String amount;

    
    private Integer baseCoinScale;

    
    private String baseSymbol;

    
    private Integer closeTimeGap;

    
    private Integer coinScale;

    
    private String coinSymbol;

    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    
    private Integer enable;

    
    private BooleanEnum enableBuy;

    
    private BooleanEnum enableSell;

    
    private BigDecimal oods;

    
    private BigDecimal feePercent;

    
    private BigDecimal initBuyReward;

    
    private BigDecimal initSellReward;

    
    private Integer maxOptionNo;

    
    private String name;

    
    private BigDecimal ngnorePercent;

    
    private Integer openTimeGap;

    
    private Integer sort;

    
    private Integer tiedType;

    
    private BigDecimal totalProfit;

    
    private Integer visible;

    
    private BigDecimal winFeePercent;


}

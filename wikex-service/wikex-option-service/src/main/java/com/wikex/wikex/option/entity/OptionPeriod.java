package com.wikex.wikex.option.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class OptionPeriod implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    
    private BigDecimal closePrice;

    
    private Long closeTime;

    
    private Long createTime;

    
    private BigDecimal initBuy;

    
    private BigDecimal initSell;

    
    private BigDecimal openPrice;

    
    private Long openTime;

    
    private Integer optionNo;

    
    private Integer result;

    
    private Integer status;

    
    private String symbol;

    
    private BigDecimal totalBuy;

    
    private Integer totalBuyCount;

    
    private BigDecimal totalPl;

    
    private BigDecimal totalSell;

    
    private Integer totalSellCount;

    
    private BigDecimal presetPrice;


}

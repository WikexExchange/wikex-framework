package com.wikex.wikex.screen;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContractOptionScreen extends PageParam{
    private String symbol; 
    private Integer optionNo; 
    private Integer totalBuyCount; 
    private Integer totalSellCount; 
    private BigDecimal totalPl; 
}

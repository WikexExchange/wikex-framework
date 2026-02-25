package com.wikex.wikex.screen;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContractOptionOrderScreen extends PageParam{
    private String symbol;
    private Long memberId;
    private BigDecimal betAmount;
    private BigDecimal rewardAmount;
}

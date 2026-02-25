package com.wikex.wikex.screen;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberWalletScreen extends PageParam{

    String unit ;

    String walletAddress ;

    BigDecimal minBalance ;

    BigDecimal maxBalance ;

    BigDecimal minFrozenBalance;

    BigDecimal maxFrozenBalance ;

    BigDecimal minAllBalance ;

    BigDecimal maxAllBalance ;

    private String account;

}

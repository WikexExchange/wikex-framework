package com.wikex.wikex.screen;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderScreen extends OtcOrderTopScreen{
    private String orderSn;
    private BigDecimal minNumber ;
    private BigDecimal maxNumber ;
    private String memberName;
    private String customerName;
    private BigDecimal minMoney;
    private BigDecimal maxMoney;
    private Integer advertiseType ;

    private Integer pageNo=1;
    private Integer pageSize=10;
}

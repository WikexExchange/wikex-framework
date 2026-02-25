package com.wikex.wikex.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CurrencyVO {
    private String fullName;
    private String imageUrl;
    private String pair;
    private BigDecimal rate;
    private String symbol;
}

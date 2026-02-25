package com.wikex.wikex.user.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MemberSymbolAmountSum {
    private Long memberId;
    private String symbol;
    private Integer type;
    private BigDecimal totalAmount;
}
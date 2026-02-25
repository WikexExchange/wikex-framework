package com.wikex.wikex.swap.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class BlastNotice {
    private Map<String, BigDecimal> plMap;
    private Long memberId;

}

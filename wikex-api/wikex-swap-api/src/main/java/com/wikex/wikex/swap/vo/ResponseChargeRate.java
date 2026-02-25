package com.wikex.wikex.swap.vo;

import lombok.Data;

import java.util.List;

@Data
public class ResponseChargeRate {

    private String status;

    private List<ChargeRateVo> data;

    private Long ts;

}

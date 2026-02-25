package com.wikex.wikex.coinswap.vo;

import lombok.Data;

import java.util.List;

@Data
public class ResponseChargeRate {

    private String status;

    private List<ChargeRateVo> data;

    private Long ts;

}

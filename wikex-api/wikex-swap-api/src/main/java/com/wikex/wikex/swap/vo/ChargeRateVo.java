package com.wikex.wikex.swap.vo;

import lombok.Data;


@Data
public class ChargeRateVo {

     private String estimated_rate;
     private String funding_rate;
     private String contract_code;
     private String symbol;
     private String fee_asset;
     private String funding_time;
     private String next_funding_time;

}

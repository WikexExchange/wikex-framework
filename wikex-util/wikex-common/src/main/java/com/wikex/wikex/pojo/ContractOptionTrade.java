package com.wikex.wikex.pojo;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.ContractOptionOrderDirection;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ContractOptionTrade implements Serializable {
    private String symbol;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal buyTurnover;
    private BigDecimal sellTurnover;
    private ContractOptionOrderDirection direction;
    private String buyOrderId;
    private String sellOrderId;
    private Long time;
    @Override
    public String toString() {
        return  JSON.toJSONString(this);
    }
}
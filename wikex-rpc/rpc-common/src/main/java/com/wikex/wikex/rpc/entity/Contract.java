package com.wikex.wikex.rpc.entity;

import com.wikex.wikex.rpc.util.EthConvert;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class Contract {
    // Contract precision (number of decimals)
    private String decimals;
    private String name;
    // Latest aggregation amount
    private BigDecimal minCollectAmount = BigDecimal.valueOf(10);
    // Contract address
    private String address;
    private BigInteger gasLimit;
    private String eventTopic0;

    public EthConvert.Unit getUnit() {
        if (StringUtils.isEmpty(decimals)) return EthConvert.Unit.ETHER;
        else return EthConvert.Unit.fromString(decimals);
    }
}

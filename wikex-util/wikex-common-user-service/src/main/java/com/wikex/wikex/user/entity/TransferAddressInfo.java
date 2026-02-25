package com.wikex.wikex.user.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Data
@Builder
public class TransferAddressInfo {
    private List<Map<String,Object>> info;
    private BigDecimal balance;
}

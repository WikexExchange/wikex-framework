package com.wikex.wikex.rpc.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Account {

    private String account;
    private String address;
    // Private key file path
    private String walletFile;
    private String privateKey;

    private BigDecimal balance = BigDecimal.ZERO;
    // Address gas balance, useful for Token and USDT
    private BigDecimal gas = BigDecimal.ZERO;
}

package com.wikex.wikex.user.vo;

import com.wikex.wikex.constant.BooleanEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Builder
@Data
public class WithdrawWalletInfo {
    private String unit;

    /**
     * Threshold
     */
    private BigDecimal threshold;

    /**
     * Minimum withdrawal amount
     */
    private BigDecimal minAmount;

    /**
     * Maximum withdrawal amount
     */
    private BigDecimal maxAmount;

    private BigDecimal minTxFee;
    private BigDecimal maxTxFee;
    private String nameCn;
    private String name;
    private BigDecimal balance;
    private BooleanEnum canAutoWithdraw;
    private int withdrawScale;
    private int accountType;

    /**
     * Addresses
     */
    private List<Map<String, String>> addresses;
}

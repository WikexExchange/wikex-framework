package com.uduncloud.sdk.domain;

import java.math.BigDecimal;

public class Trade {
    // Transaction ID
    private String txId;
    // Trade serial number
    private String tradeId;
    // Transaction address
    private String address;
    // Main coin type
    private String mainCoinType;
    // Token type, for ERC20 this is the contract address
    private String coinType;
    // Transaction amount
    private BigDecimal amount;
    // Trade type: 1 - Deposit, 2 - Withdrawal (Transfer)
    private int tradeType;
    // Trade status: 0 - Pending review, 1 - Success, 2 - Failed; deposits have no review
    private int status;
    // Miner fee
    private BigDecimal fee;
    private int decimals;
    // Withdrawal application number
    private String businessId;
    // Remark or note
    private String memo;

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMainCoinType() {
        return mainCoinType;
    }

    public void setMainCoinType(String mainCoinType) {
        this.mainCoinType = mainCoinType;
    }

    public String getCoinType() {
        return coinType;
    }

    public void setCoinType(String coinType) {
        this.coinType = coinType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getTradeType() {
        return tradeType;
    }

    public void setTradeType(int tradeType) {
        this.tradeType = tradeType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}

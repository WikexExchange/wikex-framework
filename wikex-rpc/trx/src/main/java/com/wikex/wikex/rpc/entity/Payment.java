package com.wikex.wikex.rpc.entity;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Payment {
    private String txBizNumber;
    private String txid;
    private String privateKey;
    private String to;
    private BigDecimal amount;
    private String unit;
    private Contract contract;
    private BigInteger gasLimit;
    private BigInteger gasPrice;

    public void setTxBizNumber(String txBizNumber) {
        this.txBizNumber = txBizNumber;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getTxBizNumber(){
        return txBizNumber;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }
}

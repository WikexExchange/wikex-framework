package com.wikex.wikex.rpc.entity;


import lombok.Data;
import org.springframework.util.StringUtils;
import org.web3j.tx.ChainId;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class Coin {
    private String name;
    private String unit;
    private String rpc;
    private String keystorePath;
    private BigDecimal defaultMinerFee;
    private String withdrawAddress;
    private String withdrawWallet;
    private String withdrawWalletPasswordMd5;
    private String withdrawWalletPassword;
    private BigDecimal minCollectAmount;
    private BigInteger gasLimit;
    private BigDecimal gasSpeedUp = BigDecimal.ONE;
    private BigDecimal rechargeMinerFee;
    private String ignoreFromAddress;
    private String masterAddress;
    private String withdrawWalletPrivateKey;
    private byte chainId = ChainId.ROPSTEN;

     public String getWithdrawWalletPassword() throws RuntimeException {
         if(StringUtils.isEmpty(this.withdrawWalletPassword)){
            throw new RuntimeException("Password is empty!");
         }
        return this.withdrawWalletPassword;
     }



}

package com.uduncloud.sdk.client;

import com.uduncloud.sdk.domain.Address;
import com.uduncloud.sdk.domain.Coin;
import com.uduncloud.sdk.domain.ResultMsg;
import com.uduncloud.sdk.exception.UdunException;

import java.math.BigDecimal;
import java.util.List;

public interface UdunApi {
    
    Address createAddress(String mainCoinType)  throws UdunException;

    
    Address createAddress(String mainCoinType, String alias, String walletId)  throws UdunException;

    
    Address createAddress(String mainCoinType, String alias, String walletId, String callUrl) throws UdunException;


    
    ResultMsg withdraw(String address, BigDecimal amount, String mainCoinType, String coinType, String businessId, String memo);

    
    ResultMsg withdraw(String address, BigDecimal amount, String mainCoinType, String coinType, String businessId, String memo, String callUrl);

    
    ResultMsg autoWithdraw(String address, BigDecimal amount, String mainCoinType, String coinType, String businessId, String memo);

    
    ResultMsg autoWithdraw(String address, BigDecimal amount, String mainCoinType, String coinType, String businessId, String memo, String callUrl);

    
    boolean checkAddress(String mainCoinType, String address);

    
    List<Coin> listSupportCoin(boolean showBalance);
}

package com.wikex.wikex.service;


import com.alibaba.fastjson.JSON;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.feign.CoinFeign;
import com.uduncloud.sdk.client.UdunClient;
import com.uduncloud.sdk.constant.CoinType;
import com.uduncloud.sdk.constant.MainCoinType;
import com.uduncloud.sdk.domain.Address;
import com.uduncloud.sdk.domain.ResultMsg;
import com.uduncloud.sdk.domain.Trade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class UdunService {
    @Autowired
    private UdunClient udunClient;
    @Value("${server.host}")
    private String host;
    @Value("#{'${udun.supported-coins}'.split(',')}")
    private List<String> supportedCoins;

    @Autowired
    private CoinFeign coinService;

    public boolean isSupportedCoin(String coinName) {
        return supportedCoins != null && supportedCoins.contains(coinName);
    }

    
    public Address createCoinAddress(MainCoinType coinType, String alias, String walletId) {
        String callbackUrl = host + "/wallet/notify";
        try {
            Address address = udunClient.createAddress(coinType.getCode(), callbackUrl, alias, walletId);
            return address;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public boolean withdraw(String orderId, BigDecimal amount, String mainCoinType, String subCoinType, String address) {
        String callbackUrl = host + "/wallet/notify";
        try {
            ResultMsg withdraw = udunClient.withdraw(address, amount, mainCoinType, subCoinType, orderId, "", callbackUrl);
            if(withdraw.getCode()==200){
                return true;
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }















































    public Coin convert2Coin(Trade trade) {
        String coinType = trade.getCoinType();
        if(!StringUtils.isEmpty(coinType)){
            String unit = udunClient.getUnitByCoinType(coinType);
            if(!StringUtils.isEmpty(unit)){
                return coinService.findByUnit(unit);
            }
        }
        return null;
    }

    public String getSubCodeByUnit(String unit,String mainCoinSymbol){
        return udunClient.getSubCodeByUnit(unit,mainCoinSymbol);
    }


}

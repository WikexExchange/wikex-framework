package com.wikex.wikex.user.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.CoinInfo;

public interface CoinInfoService extends IService<CoinInfo> {
    CoinInfo findByCoinId(Long coinId);

    CoinInfo fetchFromCoinGecko(String coingeckoId);

    Map<String, Object> fetchInfoToken(Long coinId);

    Map<String, Object> fetchFullInfo(String coingeckoId);
}

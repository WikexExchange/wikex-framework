package com.wikex.wikex.coinswap.feign;

import com.wikex.wikex.coinswap.engine.ContractCoinMatch;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.pojo.CoinThumb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class MarketFeignController {

    @Autowired
    private ContractCoinCoinService coinService;

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;

    
    @RequestMapping("findSymbolThumb4Feign")
    public List<CoinThumb> findSymbolThumb4Feign(){
        List<ContractCoinCoin> coins = coinService.findAllVisible();
        List<CoinThumb> thumbs = new ArrayList<>();
        for(ContractCoinCoin coin:coins){
            ContractCoinMatch processor = contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();
            thumb.setFeePercent(coin.getFeePercent());
            thumbs.add(thumb);
        }
        return thumbs;
    }
}

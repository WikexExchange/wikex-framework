package com.wikex.wikex.swap.feign;

import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.service.ContractCoinService;
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
    private ContractCoinService coinService;

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;

    
    @RequestMapping("findSymbolThumb4Feign")
    public List<CoinThumb> findSymbolThumb4Feign(){
        List<ContractCoin> coins = coinService.findAllVisible();
        List<CoinThumb> thumbs = new ArrayList<>();
        for(ContractCoin coin:coins){
            ContractCoinMatch processor = contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();
            thumb.setFeePercent(coin.getFeePercent());
            thumbs.add(thumb);
        }
        return thumbs;
    }
}

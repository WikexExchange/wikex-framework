package com.wikex.wikex.swap.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.TradingTimes;
import com.wikex.wikex.swap.service.TradingTimesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/tradingTimesFeign")
public class TradingTimesFeignController extends BaseController {
    @Autowired
    private TradingTimesService tradingTimesService;

    
    @PostMapping("findByCoinId")
    public List<TradingTimes> findByCoinId(@RequestParam("contractId") Long contractId) {
        
        List<TradingTimes> all = tradingTimesService.findByCoinId(contractId);
        return all;
    }

    

    @PostMapping("save")
    public TradingTimes save(@RequestBody TradingTimes tradingTimes) {
        
        tradingTimesService.saveOrUpdate(tradingTimes);
        return tradingTimes;
    }

    
    @PostMapping("findAll")
    public Page<TradingTimes> findAll(@RequestBody PageParam pageParam) {
        
        Page<TradingTimes> all = tradingTimesService.findAll(pageParam);
        return all;
    }
}


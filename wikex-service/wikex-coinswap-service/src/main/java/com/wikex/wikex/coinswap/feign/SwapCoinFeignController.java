package com.wikex.wikex.coinswap.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PageParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/coinFeign")
public class SwapCoinFeignController extends BaseController {
    @Autowired
    private ContractCoinCoinService coinService;

    
    @PostMapping("findAll")
    public Page<ContractCoinCoin> findAll(@RequestBody PageParam pageParam) {
        Page<ContractCoinCoin> all = coinService.findAll(pageParam);
        return all;
    }

    @PostMapping(value = "findOne")
    public ContractCoinCoin findOne(@RequestParam("contractId") Long contractId){
        return coinService.getById(contractId);
    }

    @PostMapping(value = "findBySymbol")
    public ContractCoinCoin findBySymbol(@RequestParam("symbol")String symbol){
        return coinService.findBySymbol(symbol);
    }

    
    @PostMapping("save")
    public ContractCoinCoin save(@RequestBody ContractCoinCoin contractCoin) {
        coinService.saveOrUpdate(contractCoin);
        return contractCoin;
    }
}


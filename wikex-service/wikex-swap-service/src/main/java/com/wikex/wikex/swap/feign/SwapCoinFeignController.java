package com.wikex.wikex.swap.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/coinFeign")
public class SwapCoinFeignController extends BaseController {
    @Autowired
    private ContractCoinService coinService;

    
    @PostMapping("findAll")
    public Page<ContractCoin> findAll(@RequestBody PageParam pageParam) {
        
        Page<ContractCoin> all = coinService.findAll(pageParam);
        return all;
    }





    @PostMapping(value = "findOne")
    public ContractCoin findOne(@RequestParam("contractId") Long contractId){
        return coinService.getById(contractId);
    }





    @PostMapping(value = "findBySymbol")
    public ContractCoin findBySymbol(@RequestParam("symbol")String symbol){
        return coinService.findBySymbol(symbol);
    }

    

    @PostMapping("save")
    public ContractCoin save(@RequestBody ContractCoin contractCoin) {
        
        coinService.saveOrUpdate(contractCoin);
        return contractCoin;
    }
}


package com.wikex.wikex.second.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.wikex.wikex.second.service.ContractSecondCoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("coinFeign")
public class ContractCoinFeignController extends BaseController {

    @Autowired
    private ContractSecondCoinService coinService;

    @PostMapping(value = "findAll")
    public Page<ContractSecondCoin> findAll(@RequestBody PageParam pageParam){
        return coinService.findAll(pageParam);
    }

    @PostMapping(value = "findOne")
    public ContractSecondCoin findOne(@RequestParam("id") Long id){
        return coinService.getById(id);
    }

    @PostMapping(value = "findBySymbol")
    public  ContractSecondCoin findBySymbol(@RequestParam("symbol") String symbol){
        return coinService.findBySymbol(symbol);
    }

    @PostMapping(value = "save")
    public ContractSecondCoin save(@RequestBody ContractSecondCoin contractCoin){
        coinService.saveOrUpdate(contractCoin);
        return contractCoin;
    }
}

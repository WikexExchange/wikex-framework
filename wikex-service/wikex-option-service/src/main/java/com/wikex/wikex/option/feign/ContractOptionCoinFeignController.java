package com.wikex.wikex.option.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("coinFeign")
public class ContractOptionCoinFeignController extends BaseController {

    @Autowired
    private ContractOptionCoinService coinService;

    @PostMapping(value = "findAll")
    public Page<ContractOptionCoin> findAll(@RequestBody PageParam pageParam){
        return coinService.findAll(pageParam);
    }

    @PostMapping(value = "findOneBySymbol")
    public ContractOptionCoin findOneBySymbol(@RequestParam("symbol") String symbol){
        return coinService.findBySymbol(symbol);
    }

    @PostMapping(value = "add")
    public MessageResult add(@RequestBody ContractOptionCoin contractOptionCoin){
        boolean save = coinService.save(contractOptionCoin);
        return success(save);
    }

    @PostMapping(value = "alert")
    MessageResult alert(@RequestBody ContractOptionCoin coin){
        boolean alert = coinService.updateById(coin);
        return success(alert);
    }

}

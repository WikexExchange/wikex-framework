package com.wikex.wikex.coinswap.feign;

import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;
import com.wikex.wikex.coinswap.service.ContractOrderEntrustCoinService;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.ContractOrderEntrustCoinScreen;
import com.wikex.wikex.screen.ContractOrderEntrustScreen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@Slf4j
@RestController
@RequestMapping("/orderFeign")
public class OrderFeignController extends BaseController {

    @Autowired
    private ContractOrderEntrustCoinService contractOrderEntrustService;


    @PostMapping("page-query")
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ContractOrderEntrustCoin> pageQuery(@RequestBody ContractOrderEntrustCoinScreen screen){
        return contractOrderEntrustService.pageQuery(screen);
    }

    @GetMapping("findOne")
    public ContractOrderEntrustCoin findOne(@RequestParam("id") Long id){
        return contractOrderEntrustService.getById(id);
    }

}

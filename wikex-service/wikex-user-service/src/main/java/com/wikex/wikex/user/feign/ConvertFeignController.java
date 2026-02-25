package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.screen.ConvertCoinScreen;
import com.wikex.wikex.screen.ConvertOrderScreen;
import com.wikex.wikex.user.entity.ConvertCoin;
import com.wikex.wikex.user.entity.ConvertOrder;
import com.wikex.wikex.user.service.ConvertCoinService;
import com.wikex.wikex.user.service.ConvertOrderService;
import com.wikex.wikex.user.service.MemberTransactionService;
import com.wikex.wikex.user.service.MemberWalletService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Dynamic currency exchange")
@RestController
@RequestMapping("convertFeign")
public class ConvertFeignController extends BaseController {

    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private MemberTransactionService transactionService;
    @Autowired
    private ConvertCoinService coinService;
    @Autowired
    private ConvertOrderService convertOrderService;
    @Autowired
    private MarketFeign marketFeign;

    @GetMapping("/findByCoinUnit")
    public ConvertCoin findByCoinUnit(@RequestParam("coinUnit") String coinUnit) {
        return coinService.findByCoinUnit(coinUnit);
    }

    @PostMapping(value = "/save")
    public ConvertCoin save(@RequestBody ConvertCoin convertCoin) {
        coinService.saveOrUpdate(convertCoin);
        return convertCoin;
    }

    @PostMapping(value = "/findAll")
    public Page<ConvertCoin> findAll(@RequestBody ConvertCoinScreen convertScreen) {
        return coinService.findAll(convertScreen);
    }

    @PostMapping(value = "/findOrderAll")
    public Page<ConvertOrder> findOrderAll(@RequestBody ConvertOrderScreen orderScreen) {
        return convertOrderService.findAll(orderScreen);
    }
}

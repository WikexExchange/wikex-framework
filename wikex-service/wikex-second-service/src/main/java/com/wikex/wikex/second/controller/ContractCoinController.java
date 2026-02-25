package com.wikex.wikex.second.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.wikex.wikex.second.entity.ContractSecondCycle;
import com.wikex.wikex.second.service.ContractSecondCoinService;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "Seconds Contract Trading Pairs")
@RestController
@RequestMapping("coin")
public class ContractCoinController extends BaseController {

    @Autowired
    private ContractSecondCoinService coinService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    // Get base currency
    @ApiOperation(value = "Get base currency")
    @RequestMapping("base-symbol")
    public MessageResult baseSymbol() {
        List<String> baseSymbol = coinService.getBaseSymbol();
        if (baseSymbol != null && baseSymbol.size() > 0) {
            return success(baseSymbol);
        }
        return error("baseSymbol null");
    }

    /**
     * Get contract info
     * @param symbol
     * @return
     */
    @ApiOperation(value = "Get contract info")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol")
    })
    @RequestMapping("coin-info")
    public MessageResult coinInfo(String symbol) {

        ContractSecondCoin coin = coinService.findBySymbol(symbol);
        if(coin == null) {
            return error(messageSource.getMessage("DOES_NOT_EXIST"));
        }
        return success(coin);
    }

    // Get all options contract trading pairs
    @ApiOperation(value = "Get all options contract trading pairs")
    @RequestMapping("coin-list")
    public MessageResult cointList() {
        List<ContractSecondCoin> coinList = coinService.findAllEnabled();
        return success(coinList);
    }
}

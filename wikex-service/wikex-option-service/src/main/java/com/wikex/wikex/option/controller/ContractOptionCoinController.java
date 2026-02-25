package com.wikex.wikex.option.controller;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Hevin
 * @Title: ContractOptionCoinController
 * @Description:
 * @date 2019/4/18 16:54
 */
@Api(tags = "Options Contract Trading Pairs")
@RestController
@RequestMapping("coin")
public class ContractOptionCoinController extends BaseController {

    @Autowired
    private ContractOptionCoinService coinService;

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

    // Get all options contract trading pairs
    @ApiOperation(value = "Get all options contract trading pairs")
    @RequestMapping("coin-list")
    public MessageResult cointList() {
        List<ContractOptionCoin> coinList = coinService.findAllEnabled();
        return success(coinList);
    }

    // Get option contract trading pair info
    @ApiOperation(value = "Get option contract trading pair info")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair name")
    })
    @RequestMapping("coin-info")
    public MessageResult coinInfo(String symbol) {
        ContractOptionCoin coinInfo = coinService.findBySymbol(symbol);
        return success(coinInfo);
    }
}

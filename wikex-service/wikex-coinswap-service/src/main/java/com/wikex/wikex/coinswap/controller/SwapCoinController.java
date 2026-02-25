package com.wikex.wikex.coinswap.controller;

import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * Perpetual Contract Trading Pair - Frontend Controller
 * </p>
 *
 * @author 
 * @since 2021-08-23
 */
@Api(tags = "Perpetual Contract Trading Pair")
@RestController
@RequestMapping("/coin")
public class SwapCoinController extends BaseController {
    @Autowired
    private ContractCoinCoinService coinService;
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
     * Get contract information
     * @param symbol
     * @return
     */
    @ApiOperation(value = "Get contract information")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("coin-info")
    public MessageResult coinInfo(@RequestParam("symbol") String symbol) {

        ContractCoinCoin coin = coinService.findBySymbol(symbol);
        if(coin == null) {
            return error(messageSource.getMessage("DOES_NOT_EXIST"));
        }
        return success(coin);
    }
}

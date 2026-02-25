package com.wikex.wikex.option.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ContractOptionStatus;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.option.entity.ContractOption;
import com.wikex.wikex.option.service.ContractOptionService;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Api(tags = "Options Contract Draw Records")
@RestController
@RequestMapping("option")
public class ContractOptionController extends BaseController {

    @Autowired
    private ContractOptionService optionService;

    // Get historical periods
    @ApiOperation(value = "Get Historical Periods")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
            @ApiImplicitParam(name = "count", value = "Total Count")
    })
    @RequestMapping("history")
    public MessageResult history(String symbol, int count) {
        Page<ContractOption> optionList = optionService.findAll(symbol, count);
        return success(IPage2Page(optionList));
    }

    // Get contracts currently in betting
    @ApiOperation(value = "Get Contracts Currently in Betting")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol")
    })
    @RequestMapping("starting")
    public MessageResult starting(String symbol) {
        List<ContractOption> optionList = optionService.findBySymbolAndStatus(symbol, ContractOptionStatus.STARTING);
        return success(optionList);
    }

    // Get contracts currently opening (draw in progress)
    @ApiOperation(value = "Get Contracts Currently Opening")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol")
    })
    @RequestMapping("opening")
    public MessageResult opening(String symbol) {
        List<ContractOption> optionList = optionService.findBySymbolAndStatus(symbol, ContractOptionStatus.OPENING);
        return success(optionList);
    }
}

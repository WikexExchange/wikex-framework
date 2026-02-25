package com.wikex.wikex.user.controller;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.service.CurrencyService;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Fiat Currency Exchange Rates")
@RestController
@RequestMapping("/currency")
public class CurrencyController extends BaseController {

    @Autowired
    private CurrencyService currencyService;

    @ApiOperation(value = "Get all")
    @GetMapping(value = "/findAll")
    public MessageResult findAll() {
        return success(currencyService.findAll());
    }

}

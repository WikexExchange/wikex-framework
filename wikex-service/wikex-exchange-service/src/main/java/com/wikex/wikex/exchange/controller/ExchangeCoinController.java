package com.wikex.wikex.exchange.controller;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exchange.service.ExchangeCoinService;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("exchange-coin")
public class ExchangeCoinController extends BaseController {
    @Autowired
    private ExchangeCoinService exchangeCoinService;

    @RequestMapping("base-symbol")
    public MessageResult baseSymbol() {
        List<String> baseSymbol = exchangeCoinService.getBaseSymbol();
        if (baseSymbol != null && baseSymbol.size() > 0) {
            return success(baseSymbol);
        }
        return error("baseSymbol null");
    }
}

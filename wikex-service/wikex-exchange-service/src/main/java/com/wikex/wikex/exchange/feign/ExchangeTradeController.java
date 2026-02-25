package com.wikex.wikex.exchange.feign;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exchange.service.ExchangeTradeService;
import com.wikex.wikex.pojo.ExchangeTrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("exchangeTradeFeign")
public class ExchangeTradeController extends BaseController {

    @Autowired
    private ExchangeTradeService exchangeTradeService;

    @GetMapping("/findLatest")
    public List<ExchangeTrade> findLatest(@RequestParam("symbol") String symbol, @RequestParam("size")int size){
        return exchangeTradeService.findLatest(symbol,size);
    }

}

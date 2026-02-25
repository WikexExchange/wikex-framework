package com.wikex.wikex.exchange.controller;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.service.CoinTraderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitor")
public class MonitorController {
	private Logger log = LoggerFactory.getLogger(MonitorController.class);
	@Autowired
	private CoinTraderService coinTraderService;

    @RequestMapping("trader-detail")
    public JSONObject  traderDetail(@RequestParam("symbol") String symbol){
		return coinTraderService.traderDetail(symbol);
    }

	@RequestMapping("symbols")
	public List<String> symbols() {
		return coinTraderService.symbols();

	}

	
	@RequestMapping("order")
	public ExchangeOrder findOrder(
			@RequestParam("symbol") String symbol,
			@RequestParam("orderId") String orderId,
			@RequestParam("direction")Integer direction,
			@RequestParam("type") Integer type) {
		return coinTraderService.findOrder(symbol, orderId, type, direction);
	}

}

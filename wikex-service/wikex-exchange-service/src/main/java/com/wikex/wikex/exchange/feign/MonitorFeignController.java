package com.wikex.wikex.exchange.feign;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.service.CoinTraderService;
import com.wikex.wikex.exchange.service.ExchangeCoinService;
import com.wikex.wikex.pojo.TradePlateItem;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monitorFeign")
public class MonitorFeignController {
	private Logger log = LoggerFactory.getLogger(MonitorFeignController.class);
	@Autowired
	private RocketMQTemplate rocketMQTemplate;
	@Autowired
	private ExchangeCoinService exchangeCoinService;
	@Autowired
	private LocaleMessageSourceService msService;
	@Autowired
	private CoinTraderService coinTraderService;

	@RequestMapping("engines")
	public Map<String, Integer> engines() {
		return coinTraderService.engines();
	}

	@RequestMapping("plate")
	public Map<String, List<TradePlateItem>> traderPlate(@RequestParam("symbol") String symbol) {
		return coinTraderService.plate(symbol);
	}

	@RequestMapping("plate-mini")
	public Map<String, JSONObject> traderPlateMini(@RequestParam("symbol") String symbol) {
		return coinTraderService.plateMini(symbol);
	}

	@RequestMapping("plate-full")
	public Map<String, JSONObject> traderPlateFull(String symbol) {
		return coinTraderService.plateFull(symbol);
	}

	@RequestMapping("reset-trader")
	public MessageResult resetTrader(@RequestParam("symbol")String symbol) {
		
		if(coinTraderService.containsTrader(symbol)) {
			ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
			if (coin == null || coin.getEnable() != 1) {
				return MessageResult.error(500, "CURRENCY_PAIR_DOES_NOT_EXIST");
			}

			if(coin.getEnable() != 1) {
				return MessageResult.error(500, "PROHIBITION_OF_CURRENCY_PAIRS");
			}
			coinTraderService.resetTrader(symbol);
			
			return MessageResult.success(symbol+ msService.getMessage("ENGINE_CREATED_SUCCESSFULLY"));
		}else {
			return MessageResult.error(500, symbol + msService.getMessage("ENGINE_DOES_NOT_EXIST"));
		}
	}

	@RequestMapping("stop-trader")
	public MessageResult stopTrader(String symbol) {
		Boolean tradingHalt = coinTraderService.getTradingStatus(symbol);
		
		if(tradingHalt == null) {
			return MessageResult.error(500, symbol + msService.getMessage("CURRENCY_PAIR_ENGINE_DOES_NOT_EXIST"));
		}else {
			if(tradingHalt) {
				return MessageResult.error(500, symbol + msService.getMessage("ENGINE_STATE_HAS_STOPPED"));
			}else {
				coinTraderService.stopTrader(symbol);
				return MessageResult.success("ENGINE_STOPPED_SUCCESSFULLY");
			}
		}
	}

	@RequestMapping("overview")
	public JSONObject traderOverview(@RequestParam("symbol") String symbol){
		return coinTraderService.traderOverview(symbol);
	}

	@RequestMapping("start-trader")
	public MessageResult startTrader(@RequestParam("symbol")String symbol) {
		return coinTraderService.startTrader(symbol);
	}

	@RequestMapping("order4Feign")
	public ExchangeOrder order4Feign(@RequestBody ExchangeOrder order) {
		return this.findOrder(order.getSymbol(), order.getOrderId(), order.getDirection().getCode(),order.getType().getCode());
	}
	
	private ExchangeOrder findOrder(
			@RequestParam("symbol") String symbol,
			@RequestParam("orderId") String orderId,
			@RequestParam("direction")Integer direction,
			@RequestParam("type") Integer type) {
		return coinTraderService.findOrder(symbol,orderId, type, direction);
	}
}

package com.wikex.wikex.match.feign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ExchangeCoinPublishType;
import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.exchange.entity.TradePlate;
import com.wikex.wikex.exchange.util.OrderUtils;
import com.wikex.wikex.match.service.ExchangeCoinService;
import com.wikex.wikex.match.service.ExchangeOrderDetailService;
import com.wikex.wikex.match.service.ExchangeOrderService;
import com.wikex.wikex.match.trader.CoinTrader;
import com.wikex.wikex.match.trader.CoinTraderFactory;
import com.wikex.wikex.pojo.TradePlateItem;
import com.wikex.wikex.util.MessageResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@RestController
@RequestMapping("/matchFeign")
public class MatchFeignController {
	private Logger log = LoggerFactory.getLogger(MatchFeignController.class);
	@Autowired
	private CoinTraderFactory factory;
	@Autowired
	private RocketMQTemplate rocketMQTemplate;
	@Autowired
	private ExchangeCoinService exchangeCoinService;
	@Autowired
	private ExchangeOrderService exchangeOrderService;
	@Autowired
	private ExchangeOrderDetailService exchangeOrderDetailService;

	@GetMapping("plate")
	public Map<String, List<TradePlateItem>> traderPlate(@RequestParam("symbol") String symbol) {
		try {
			CoinTrader trader = factory.getTrader(symbol);
			if (trader == null) {
				return Collections.emptyMap();
			}

			TradePlate buy = trader.getTradePlate(ExchangeOrderDirection.BUY);
			TradePlate sell = trader.getTradePlate(ExchangeOrderDirection.SELL);

			Map<String, List<TradePlateItem>> result = new HashMap<>();
			result.put("bid", buy != null && buy.getItems() != null ? buy.getItems() : Collections.emptyList());
			result.put("ask", sell != null && sell.getItems() != null ? sell.getItems() : Collections.emptyList());

			return result;
		} catch (Exception ex) {
			log.error("Failed matchFeign traderPlate, symbol={}", symbol, ex);
		}
		return Collections.emptyMap();
	}

	@RequestMapping(
		value = "plate-mini",
		produces = MediaType.APPLICATION_JSON_VALUE
	)
	public Map<String, JSONObject> traderPlateMini(@RequestParam("symbol") String symbol) {
		try {
			CoinTrader trader = factory.getTrader(symbol);
			if (trader == null) {
				return Collections.emptyMap();
			}
			TradePlate buy = trader.getTradePlate(ExchangeOrderDirection.BUY);
			TradePlate sell = trader.getTradePlate(ExchangeOrderDirection.SELL);

			Map<String, JSONObject> result = new HashMap<>();
			result.put("bid", buy != null ? buy.toJSON(30) : new JSONObject());
			result.put("ask", sell != null ? sell.toJSON(30) : new JSONObject());
			return result;
		} catch (Exception ex) {
			log.error("Failed matchFeign traderPlateMini, symbol={}", symbol, ex);
		}
		return Collections.emptyMap();
	}

	@GetMapping(
		value = "plate-full",
		produces = MediaType.APPLICATION_JSON_VALUE
	)
	Map<String, JSONObject> plateFull( @RequestParam("symbol")String symbol){
		try {
			CoinTrader trader = factory.getTrader(symbol);
			if (trader == null) {
				return Collections.emptyMap();
			}
			TradePlate buy = trader.getTradePlate(ExchangeOrderDirection.BUY);
			TradePlate sell = trader.getTradePlate(ExchangeOrderDirection.SELL);

			Map<String, JSONObject> result = new HashMap<>();
			result.put("bid", buy != null ? buy.toJSON(100) : new JSONObject());
			result.put("ask", sell != null ? sell.toJSON(100) : new JSONObject());
			return result;
		} catch (Exception ex) {
			log.error("Failed matchFeign plateFull, symbol={}", symbol, ex);
		}
		return Collections.emptyMap();
	}
	@RequestMapping("engines")
	public Map<String, Integer> engines() {
		Map<String, Integer> symbols = new HashMap<String, Integer>();
		try {
			Map<String, CoinTrader> traders = factory.getTraderMap();
			traders.forEach((key, trader) -> {
				if (trader.isTradingHalt()) {
					symbols.put(key, 2);
				} else {
					symbols.put(key, 1);
				}
			});
			return symbols;
		} catch (Exception ex) {
			log.error("Failed matchFeign engines: ", ex);
		}
		return symbols;
	}


	@RequestMapping("containsTrader")
	public Boolean containsTrader(@RequestParam("symbol") String symbol) {
		return factory.containsTrader(symbol);
	}

	@RequestMapping("addTrader")
	public void addTrader(@RequestParam("symbol") String symbol) {
		try {
			ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
			if (coin == null || coin.getEnable() != 1) {
				return;
			}
			CoinTrader newTrader = buildTrader(coin);


			List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(symbol);
			List<ExchangeOrder> tradingOrders = new ArrayList<>();
			List<ExchangeOrder> completedOrders = new ArrayList<>();
			orders.forEach(order -> {
				BigDecimal tradedAmount = BigDecimal.ZERO;
				BigDecimal turnover = BigDecimal.ZERO;
				List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(order.getOrderId());

				for (ExchangeOrderDetail od : details) {
					tradedAmount = tradedAmount.add(od.getAmount());
					turnover = turnover.add(od.getAmount().multiply(od.getPrice()));
				}
				order.setTradedAmount(tradedAmount);
				order.setTurnover(turnover);
				if (!OrderUtils.isCompleted(order)) {
					tradingOrders.add(order);
				} else {
					completedOrders.add(order);
				}
			});
			try {
				newTrader.trade(tradingOrders);
			} catch (ParseException e) {
				log.error("Failed to build trader, symbol={}", symbol, e);
			}
			if (completedOrders.size() > 0) {
				rocketMQTemplate.convertAndSend("exchange-order-completed", JSON.toJSONString(completedOrders));
			}
			newTrader.setReady(true);
			factory.addTrader(symbol, newTrader);
		} catch (Exception e) {
			log.error("Failed to build trader", e);
		}
	}

	@RequestMapping("findOrder")
	ExchangeOrder findOrder(@RequestParam("symbol")String symbol,
							@RequestParam("orderId")String orderId, @RequestParam("type")Integer type,
							@RequestParam("direction")Integer direction){
		CoinTrader trader = factory.getTrader(symbol);
		if(trader == null) {
			return null;
		}
		return trader.findOrder(orderId, type, direction);
	}

	@RequestMapping("start-trader")
	public MessageResult startTrader(@RequestParam("symbol")String symbol) {
		if(!factory.containsTrader(symbol)) {
			ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
			if (coin == null || coin.getEnable() != 1) {
				return MessageResult.error(500, "CURRENCY_PAIR_DOES_NOT_EXIST");
			}
			if(coin.getEnable() != 1) {
				return MessageResult.error(500, "PROHIBITION_OF_CURRENCY_PAIRS");
			}
			CoinTrader newTrader = buildTrader(coin);
			
			List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(symbol);
			List<ExchangeOrder> tradingOrders = new ArrayList<>();
			List<ExchangeOrder> completedOrders = new ArrayList<>();
			orders.forEach(order -> {
				BigDecimal tradedAmount = BigDecimal.ZERO;
				BigDecimal turnover = BigDecimal.ZERO;
				List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(order.getOrderId());

				for(ExchangeOrderDetail od:details){
					tradedAmount = tradedAmount.add(od.getAmount());
					turnover = turnover.add(od.getAmount().multiply(od.getPrice()));
				}
				order.setTradedAmount(tradedAmount);
				order.setTurnover(turnover);
				if(!OrderUtils.isCompleted(order)){
					tradingOrders.add(order);
				}
				else{
					completedOrders.add(order);
				}
			});
			try {
				newTrader.trade(tradingOrders);
			} catch (ParseException e) {
				log.error("Failed to start trader, symbol={}", symbol, e);
				return MessageResult.error(500, "ENGINE_CREATION_FAILED");
			}
			
			if(completedOrders.size() > 0){
				rocketMQTemplate.convertAndSend("exchange-order-completed", JSON.toJSONString(completedOrders));
			}
			newTrader.setReady(true);
			factory.addTrader(symbol, newTrader);

			return MessageResult.success("CURRENCY_PAIR_CREATED_SUCCESSFULLY");
		}else {
			CoinTrader trader= factory.getTrader(symbol);
			if(trader.isTradingHalt()) {
				trader.resumeTrading();
				return MessageResult.success("ENGINE_STATE_HAS_STOPPED");
			}else {
				return MessageResult.error(500, "ENGINE_STATUS_IS_RUNNING");
			}
		}
	}


	@GetMapping("getTradingStatus")
	Boolean getTradingStatus(@RequestParam("symbol")String symbol){
		return factory.getTrader(symbol).isTradingHalt();
	}
	@GetMapping("stopTrader")
	void stopTrader(@RequestParam("symbol")String symbol) {
		CoinTrader trader = factory.getTrader(symbol);
		if(trader == null) {
			return ;
		}
		trader.haltTrading();
	}

	@RequestMapping("traderOverview")
	public JSONObject traderOverview(@RequestParam("symbol") String symbol){
		CoinTrader trader = factory.getTrader(symbol);
		if(trader == null ) {
			return null;
		}
		JSONObject result = new JSONObject();
		
		JSONObject ask = new JSONObject();
		
		JSONObject bid = new JSONObject();
		ask.put("limit_price_order_count",trader.getLimitPriceOrderCount(ExchangeOrderDirection.SELL));
		ask.put("market_price_order_count",trader.getSellMarketQueue().size());
		ask.put("depth",trader.getTradePlate(ExchangeOrderDirection.SELL).getDepth());
		bid.put("limit_price_order_count",trader.getLimitPriceOrderCount(ExchangeOrderDirection.BUY));
		bid.put("market_price_order_count",trader.getBuyMarketQueue().size());
		bid.put("depth",trader.getTradePlate(ExchangeOrderDirection.BUY).getDepth());
		result.put("ask",ask);
		result.put("bid",bid);
		return result;
	}

	@GetMapping("resetTrader")
	void resetTrader( @RequestParam("symbol")String symbol){
		ExchangeCoin coin = exchangeCoinService.findBySymbol(symbol);
		if (coin == null || coin.getEnable() != 1) {
			return ;
		}

		if(coin.getEnable() != 1) {
			return ;
		}
		CoinTrader trader= factory.getTrader(symbol);
		if(!trader.isTradingHalt()) {
			return ;
		}
		CoinTrader newTrader = buildTrader(coin);

		
		
		List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(symbol);
		List<ExchangeOrder> tradingOrders = new ArrayList<>();
		List<ExchangeOrder> completedOrders = new ArrayList<>();
		orders.forEach(order -> {
			BigDecimal tradedAmount = BigDecimal.ZERO;
			BigDecimal turnover = BigDecimal.ZERO;
			List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(order.getOrderId());

			for(ExchangeOrderDetail od:details){
				tradedAmount = tradedAmount.add(od.getAmount());
				turnover = turnover.add(od.getAmount().multiply(od.getPrice()));
			}
			order.setTradedAmount(tradedAmount);
			order.setTurnover(turnover);
			if(!OrderUtils.isCompleted(order)){
				tradingOrders.add(order);
			}
			else{
				completedOrders.add(order);
			}
		});
		try {
			newTrader.trade(tradingOrders);
		} catch (ParseException e) {
			log.error("Failed to reset trader, symbol={}", symbol, e);
		}
		
		if(completedOrders.size() > 0){
			rocketMQTemplate.convertAndSend("exchange-order-completed", JSON.toJSONString(completedOrders));
		}
		newTrader.setReady(true);
		factory.resetTrader(symbol, newTrader);

	}

	private CoinTrader buildTrader(ExchangeCoin coin) {
		CoinTrader newTrader = new CoinTrader(coin.getSymbol());
		newTrader.setRocketMQTemplate(rocketMQTemplate);
		newTrader.setBaseCoinScale(coin.getBaseCoinScale());
		newTrader.setCoinScale(coin.getCoinScale());
		newTrader.setPublishType(ExchangeCoinPublishType.creator(coin.getPublishType()));
		newTrader.setClearTime(coin.getClearTime());
		
		// Set price threshold from ExchangeCoin config
		// If not configured (null or <= 0), use default values (0.8 for BID, 1.2 for ASK)
		BigDecimal bidThreshold = coin.getBidPriceThreshold();
		BigDecimal askThreshold = coin.getAskPriceThreshold();
		newTrader.setPriceThreshold(bidThreshold, askThreshold);
		
		return newTrader;
	}

	@RequestMapping("trader-detail")
	public JSONObject traderDetail(@RequestParam("symbol") String symbol){
		CoinTrader trader = factory.getTrader(symbol);
		if(trader == null ) {
			return null;
		}
		JSONObject result = new JSONObject();
		
		JSONObject ask = new JSONObject();
		
		JSONObject bid = new JSONObject();
		ask.put("limit_price_queue",trader.getSellLimitPriceQueue());
		ask.put("market_price_queue",trader.getSellMarketQueue());
		bid.put("limit_price_queue",trader.getBuyLimitPriceQueue());
		bid.put("market_price_queue",trader.getBuyMarketQueue());
		result.put("ask",ask);
		result.put("bid",bid);
		return result;
	}

	@GetMapping("symbols")
	List<String> symbols(){
		List<String> result = new ArrayList<>();
		Map<String, CoinTrader> traders = factory.getTraderMap();
		traders.forEach((key, trader) -> {
			result.add(key);
		});
		return result;
	}
}

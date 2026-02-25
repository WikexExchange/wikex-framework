package com.wikex.wikex.robot.normal.robot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.constant.ExchangeOrderType;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.exchange.feign.MonitorFeign;
import com.wikex.wikex.robot.market.feign.RobotMarketFeign;
import com.wikex.wikex.robot.normal.entity.ExchangeOrderBean;
import com.wikex.wikex.robot.normal.entity.RobotParams;
import com.wikex.wikex.robot.normal.service.CustomRobotKlineService;
import com.wikex.wikex.robot.normal.service.RobotParamService;
import com.wikex.wikex.robot.normal.vo.RobotNormalCleanVo;
import com.wikex.wikex.util.MessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.wikex.wikex.robot.normal.utils.JSONUtils.getJsonObject;

public class ExchangeRobotNormal implements Runnable, ExchangeRobot {
	private final int millis = 150;
	public Logger logger = LoggerFactory.getLogger(ExchangeRobotNormal.class);
	private AtomicBoolean running = new AtomicBoolean(true);

	private RobotMarketFeign robotMarketFeign;

	private ExchangeOrderFeign exchangeOrderFeign;

	private MonitorFeign monitorFeign;

	private RobotParamService robotParamService;

	protected String plateSymbol;
	protected RobotParams robotParams;

	protected int uid = 1;
	protected String sign = "77585211314qazwsx";

	protected String exchangeTYPE = "LIMIT_PRICE";
	protected String exchangeTYPEMarket = "MARKET_PRICE";
	protected String directionSELL = "SELL";

	protected Random rand = new Random();

	protected Instant lastSendOrderTime = Instant.now();

	public BigDecimal currentPrice;

	public String getPlateSymbol() {
		return plateSymbol;
	}

	@Override
	public void setPlateSymbol(String plateSymbol) {
		this.plateSymbol = plateSymbol;
	}

	@Override
	public RobotParams getRobotParams() {
		return robotParams;
	}

	@Override
	public void setRobotParams(RobotParams robotParams) {
		this.robotParams = robotParams;

		robotParamService.update(robotParams.getCoinName(), robotParams);
	}

	@Override
	public void updateRobotParams(RobotParams params) {
		this.robotParams.setStartAmount(params.getStartAmount());
		this.robotParams.setRandRange0(params.getRandRange0());
		this.robotParams.setRandRange1(params.getRandRange1());
		this.robotParams.setRandRange2(params.getRandRange2());
		this.robotParams.setRandRange3(params.getRandRange3());
		this.robotParams.setRandRange4(params.getRandRange4());
		this.robotParams.setRandRange5(params.getRandRange5());
		this.robotParams.setRandRange6(params.getRandRange6());
		this.robotParams.setScale(params.getScale());
		this.robotParams.setAmountScale(params.getAmountScale());
		this.robotParams.setMaxSubPrice(params.getMaxSubPrice());
		this.robotParams.setInitOrderCount(params.getInitOrderCount());
		this.robotParams.setPriceStepRate(params.getPriceStepRate());
		this.robotParams.setRunTime(params.getRunTime());
		this.robotParams.setRobotType(0);
		this.robotParams.setStrategyType(params.getStrategyType());
		this.robotParams.setFlowPair(params.getFlowPair());
		this.robotParams.setFlowPercent(params.getFlowPercent());
		this.robotParams.setMaxRandomPrice(params.getMaxRandomPrice());
		this.robotParams.setAbsolutePriceStep(params.getAbsolutePriceStep());
		robotParamService.update(this.plateSymbol, params);
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}

	private Map<BigDecimal, List<ExchangeOrder>> groupOrdersByPrice(List<ExchangeOrder> orders) {
		Map<BigDecimal, List<ExchangeOrder>> map = new HashMap<>();

		for (ExchangeOrder order : orders) {
			BigDecimal price = getScalePrice(order.getPrice());
			if (!map.containsKey(price)) {
				map.put(price, new ArrayList<>());
			}
			List<ExchangeOrder> list = map.get(price);
			list.add(order);
		}

		return map;
	}

	private List<ExchangeOrder> cleanupTooManyOrdersAtSamePrice(List<ExchangeOrder> orders, boolean isBuy) {
		List<ExchangeOrder> remainOrders = new ArrayList<>();
		List<ExchangeOrder> removedOrders = new ArrayList<>();
		// Group by price
		Map<BigDecimal, List<ExchangeOrder>> grouped = this.groupOrdersByPrice(orders);
		List<BigDecimal> priceList = new ArrayList<>(grouped.keySet());
		if (isBuy) {
			Collections.sort(priceList, Collections.reverseOrder());
		} else {
			Collections.sort(priceList);
		}

		int[] keepRules = { 1 };
		for (int i = 0; i < priceList.size(); i++) {
			BigDecimal price = priceList.get(i);
			List<ExchangeOrder> samePriceOrders = grouped.get(price);

			if (samePriceOrders == null || samePriceOrders.isEmpty()) {
				continue;
			}

			int keepCount = (i < keepRules.length) ? keepRules[i] : 1;
			samePriceOrders.sort(Comparator.comparing(ExchangeOrder::getAmount).reversed());
			for (int j = 0; j < samePriceOrders.size(); j++) {
				if (j < keepCount) {
					remainOrders.add(samePriceOrders.get(j));
				} else {
					removedOrders.add(samePriceOrders.get(j));
				}
			}
		}

		if (removedOrders.size() > 0) {
			for (int i = 0; i < removedOrders.size(); i++) {
				ExchangeOrder toCancel = removedOrders.get(i);
				this.cancelCurrentOrder(toCancel.getOrderId());
				sleep(100);
			}
		}
		return remainOrders;
	}

	public RobotNormalCleanVo processCurrentOrder() {
		int moreThanOrder = 0;
		List<BigDecimal> askList0 = new ArrayList<>();
		List<BigDecimal> bidList0 = new ArrayList<>();
		try {
			Map<String, JSONObject> plateResult1 = getPlateResult(this.plateSymbol);
			if (plateResult1 != null && !plateResult1.isEmpty()) {
				int codeBUY = ExchangeOrderDirection.BUY.getCode();
				int codeSELL = ExchangeOrderDirection.SELL.getCode();
				JSONArray askList = plateResult1.get("ask").getJSONArray("items");
				JSONArray bidList = plateResult1.get("bid").getJSONArray("items");

				Page<ExchangeOrder> exchangeOrders = exchangeOrderFeign.currentOrderMock(Long.valueOf(this.uid), this.sign, this.plateSymbol, 0, 400);
				int orderRetry = 0;
				while (exchangeOrders == null || exchangeOrders.getRecords() == null) {
					try {
						if (orderRetry > 3) {
							break;
						}
						Thread.sleep(500);
						exchangeOrders = exchangeOrderFeign.currentOrderMock(Long.valueOf(this.uid), this.sign, this.plateSymbol, 0, 400);
					} catch (Exception e) {
						break;
					}
					orderRetry += 1;
				}

				List<ExchangeOrder> buyOrderList = new ArrayList<>();
				List<ExchangeOrder> sellOrderList = new ArrayList<>();
				if (exchangeOrders != null) {
					List<ExchangeOrder> orderList = exchangeOrders.getRecords();
					if (orderList != null && orderList.size() > 0) {
						if (orderList.size() >= 250)
							moreThanOrder = 1;
						for (ExchangeOrder order : orderList) {
							if (order.getDirection().getCode() == codeBUY) {
								buyOrderList.add(order);
							} else if (order.getDirection().getCode() == codeSELL) {
								sellOrderList.add(order);
							} else {
								this.cancelCurrentOrder(order.getOrderId());
								Thread.sleep(100);
							}
						}
					}
				}

				if (askList.size() == 0 || bidList.size() == 0) {
					if (buyOrderList.size() > 0) {
						for (int j = 0; j < buyOrderList.size(); j++) {
							this.cancelCurrentOrder(buyOrderList.get(j).getOrderId());
							Thread.sleep(100);
						}
					}
					if (sellOrderList.size() > 0) {
						for (int k = 0; k < sellOrderList.size(); k++) {
							this.cancelCurrentOrder(sellOrderList.get(k).getOrderId());
							Thread.sleep(100);
						}
					}
				} else {
					List<String> buyCancels = new ArrayList<>();
					List<String> buyOldCancels = new ArrayList<>();
					buyOrderList = this.cleanupTooManyOrdersAtSamePrice(buyOrderList, true);
					if (buyOrderList.size() > this.robotParams.getInitOrderCount()) {
						buyOrderList = buyOrderList.stream().sorted(Comparator.comparing(ExchangeOrder::getPrice).reversed()).collect(Collectors.toList());
						for (int j = 0; j < buyOrderList.size(); j++) {
							if (j > 35) {
								buyCancels.add(buyOrderList.get(j).getOrderId());
							} else if (j < 15 && j % 3 == 0) {
								buyOldCancels.add(buyOrderList.get(j).getOrderId());
							} else {
								bidList0.add(getScalePrice(buyOrderList.get(j).getPrice()));
							}
						}
					}

					List<String> sellCancels = new ArrayList<>();
					List<String> sellOldCancels = new ArrayList<>();
					sellOrderList = this.cleanupTooManyOrdersAtSamePrice(sellOrderList, false);
					if (sellOrderList.size() > this.robotParams.getInitOrderCount()) {
						sellOrderList = sellOrderList.stream().sorted(Comparator.comparing(ExchangeOrder::getPrice)).collect(Collectors.toList());
						for (int k = 0; k < sellOrderList.size(); k++) {
							if (k > 35) {
								sellCancels.add(sellOrderList.get(k).getOrderId());
							} else if (k < 15 && k % 3 == 0) {
								sellOldCancels.add(sellOrderList.get(k).getOrderId());
							} else {
								askList0.add(getScalePrice(sellOrderList.get(k).getPrice()));
							}
						}
					}

					if (buyCancels.size() > 0) {
						for (int i = 0; i < buyCancels.size(); i++){
							this.cancelCurrentOrder(buyCancels.get(i));
							Thread.sleep(100);
						}
					}
					if (sellCancels.size() > 0) {
						for (int i = 0; i < sellCancels.size(); i++){
							this.cancelCurrentOrder(sellCancels.get(i));
							Thread.sleep(100);
						}
					}
					if (buyOldCancels.size() > 0) {
						for (int i = buyOldCancels.size() - 1; i >= 0; i--){
							this.cancelCurrentOrder(buyOldCancels.get(i));
							Thread.sleep(100);
						}
					}
					if (sellOldCancels.size() > 0) {
						for (int i = sellOldCancels.size() - 1; i >= 0; i--){
							this.cancelCurrentOrder(sellOldCancels.get(i));
							Thread.sleep(100);
						}
					}
				}
			}
		} catch (InterruptedException e) {
			logger.error("Robot exception: processCurrentOrder3: " + e);
		} catch (Exception e) {
			logger.error("Robot exception：processCurrentOrder4: " + e);
		}

		RobotNormalCleanVo vo = new RobotNormalCleanVo();
		vo.setMoreThanOrder(moreThanOrder);
		vo.setAskList(askList0);
		vo.setBidList(bidList0);
		return vo;
	}

	public void cancelCurrentOrder(String orderId) {
		try {
			exchangeOrderFeign.cancelOrdermock(Long.valueOf(this.uid), this.sign, orderId);
		} catch (Exception e) {
			logger.error("Robot exception：buildSingleOrder: ", e);
		}
	}

	private BigDecimal randomAroundMid(BigDecimal midPrice, BigDecimal spreadPercent) {
		BigDecimal tickSize = calcTickSize(midPrice);
		BigDecimal maxDelta = midPrice.multiply(spreadPercent);

		int maxTicks = maxDelta.divide(tickSize, 0, RoundingMode.DOWN).intValue();

		int randomTick = ThreadLocalRandom.current().nextInt(-maxTicks, maxTicks + 1);

		BigDecimal price = midPrice.add(tickSize.multiply(BigDecimal.valueOf(randomTick)));

		return getScalePrice(price);
	}

	public BigDecimal generateRandomPrice(BigDecimal currentPrice) {
		BigDecimal range = BigDecimal.valueOf(this.robotParams.getMaxRandomPrice());
		int scale = this.robotParams.getScale();
		if (currentPrice == null || range == null || scale < 0) {
			throw new IllegalArgumentException("Parameters cannot be null, and the number of decimal places must be non-negative");
		}

		double randomValue = -range.doubleValue() + (2 * range.doubleValue()) * this.rand.nextDouble();

		BigDecimal randomDelta = new BigDecimal(randomValue);

		BigDecimal randomPrice = currentPrice.add(randomDelta);

		randomPrice = randomPrice.setScale(scale, RoundingMode.HALF_UP);

		return randomPrice;
	}

	public static void main(String[] args) {

	}

	protected void buildSingleOrder(String direction, BigDecimal price, BigDecimal amount) {
		ExchangeOrderBean orderBean = new ExchangeOrderBean();
		orderBean.setSymbol(this.plateSymbol);
		orderBean.setPrice(price);
		orderBean.setAmount(amount);
		orderBean.setDirection(direction);
		orderBean.setType(this.exchangeTYPE);
		orderBean.setUid(this.uid);
		orderBean.setUseDiscount(0);
		orderBean.setSign(this.sign);

		this.sendOrder(orderBean);

		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.error("Robot exception：buildSingleOrder: " + e);
		}
	}

	protected void buildInitPlateOrder(int count, BigDecimal lastestPrice) {
		List<BigDecimal> exitsAskPrices = new ArrayList<>();
		List<BigDecimal> addAskPrices = this.buildAskBook(exitsAskPrices, lastestPrice, count);
		List<BigDecimal> exitsBidPrices = new ArrayList<>();
		List<BigDecimal> addBidPrices = this.buildBidBook(exitsBidPrices, lastestPrice, count);

		List<ExchangeOrderBean> buy_orders = new ArrayList<>();
		List<ExchangeOrderBean> sell_orders = new ArrayList<>();
		if (addAskPrices.size() > 0) {
			sell_orders = this.buildOrderNew("SELL", addAskPrices);
		}
		if (addBidPrices.size() > 0) {
			buy_orders = this.buildOrderNew("BUY", addBidPrices);
		}

		int buy_count = buy_orders.size();
		int sell_count = sell_orders.size();
		if (buy_count > 0 || sell_count > 0) {
			if (buy_count >= sell_count) {
				for (int i = 0; i < buy_count; i++) {
					this.sendOrder(buy_orders.get(i));
					sleep(100);
					if (i < sell_count) {
						this.sendOrder(sell_orders.get(i));
						sleep(100);
					}
				}
			} else {
				for (int i = 0; i < sell_count; i++) {
					this.sendOrder(sell_orders.get(i));
					sleep(100);
					if (i < buy_count) {
						this.sendOrder(buy_orders.get(i));
						sleep(100);
					}
				}
			}
		}
	}

	private BigDecimal getScalePrice(BigDecimal price) {
		return price.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
	}

	private double getAmount(int index, List<Double> list) {
		list.sort(Collections.reverseOrder());
		int middle = list.size() / 2;
		int mappedIndex = middle - index;
		if (mappedIndex < 0)
			mappedIndex = -mappedIndex;
		return list.get(mappedIndex);
	}

	private boolean isExitsPrice(List<BigDecimal> prices, BigDecimal price) {
		if (price.compareTo(BigDecimal.ZERO) <= 0)
			return true;

		for (BigDecimal p : prices) {
			if (p.compareTo(price) == 0) {
				return true;
			}
		}
		return false;
	}

	protected List<ExchangeOrderBean> buildOrder(String direction, int count, BigDecimal lastestPrice, boolean initFlag) {
		BigDecimal stepPercent = this.robotParams.getPriceStepRate();

		List<ExchangeOrderBean> list_orders = new ArrayList<>();
		int maxCount = count * 2;
		List<BigDecimal> exit_prices = new ArrayList<>();
		List<Double> temRands = getTemRand(count);
		for (int i = 0; i < count; i++) {
			double temRand = getAmount(i, temRands);
			BigDecimal amount = this.scaleAmount(temRand);

			double randStepFactor = 0.8 + (1.4 - 0.8) * this.rand.nextDouble();
			BigDecimal dynamicStep = lastestPrice
					.multiply(stepPercent)
					.multiply(BigDecimal.valueOf(randStepFactor));

			double gaussianNoise = this.rand.nextGaussian() * 0.3; // 30% độ lệch chuẩn
			BigDecimal gaussianDelta = dynamicStep.multiply(BigDecimal.valueOf(gaussianNoise));

			double drift = 0.5 - this.rand.nextDouble(); // -0.5 đến 0.5
			BigDecimal offset = dynamicStep.multiply(BigDecimal.valueOf(i + 1 + drift));

			BigDecimal price;
			if (direction.equals("SELL")) {
				price = lastestPrice.add(offset).add(gaussianDelta);
			} else {
				price = lastestPrice.subtract(offset).add(gaussianDelta.negate());
			}
			price = price.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
			if (price.compareTo(BigDecimal.ZERO) <= 0)
				continue;

			boolean exists = false;
			for (BigDecimal p : exit_prices) {
				if (p.compareTo(price) == 0) {
					exists = true;
					break;
				}
			}
			if (exists && count < maxCount) {
				count += 1;
				continue;
			}

			exit_prices.add(price);
			ExchangeOrderBean orderBean = new ExchangeOrderBean();
			orderBean.setSymbol(this.plateSymbol);
			orderBean.setPrice(price);
			orderBean.setAmount(amount);
			orderBean.setDirection(direction);
			orderBean.setType(this.exchangeTYPE);
			orderBean.setUid(this.uid);
			orderBean.setUseDiscount(0);
			orderBean.setSign(this.sign);
			list_orders.add(orderBean);
			sleep(10);
		}
		return list_orders;
	}

	protected void buildSpecOrder(String direction, BigDecimal startPrice, BigDecimal endPrice) {
		if (startPrice.compareTo(endPrice) > 0) {
			return;
		}

		int count = 3;
		int maxCount = 6;
		List<BigDecimal> exit_prices = new ArrayList<>();
		List<Double> temRands = getTemRand(count);
		for (int i = 0; i < count; i++) {
			double temRand = getAmount(i, temRands);
			BigDecimal amount = this.scaleAmount(temRand);
			BigDecimal price = generateRandomPrice(endPrice);
			if (price.compareTo(BigDecimal.ZERO) <= 0)
				continue;

			boolean exists = false;
			for (BigDecimal p : exit_prices) {
				if (p.compareTo(price) == 0) {
					exists = true;
					break;
				}
			}
			if (exists && count < maxCount) {
				count += 1;
				continue;
			}

			exit_prices.add(price);
			ExchangeOrderBean orderBean = new ExchangeOrderBean();
			orderBean.setSymbol(this.plateSymbol);
			orderBean.setPrice(price);
			orderBean.setAmount(amount);
			orderBean.setDirection(direction);
			orderBean.setType(this.exchangeTYPE);
			orderBean.setUid(this.uid);
			orderBean.setUseDiscount(0);
			orderBean.setSign(this.sign);

			this.sendOrder(orderBean);

			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				logger.error("Robot exception：buildSpecOrder: " + e);
			}
		}
	}

	private List<Double> getTemRand(int count) {
		List<Double> list = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			double temRand = 0.1;
			int intRand = this.rand.nextInt(100);
			if (intRand >= 0 && intRand < 1) {
				temRand = this.robotParams.getRandRange0();
			} else if (intRand >= 1 && intRand < 10) {
				temRand = this.robotParams.getRandRange1();
			} else if (intRand >= 10 && intRand < 30) {
				temRand = this.robotParams.getRandRange2();
			} else if (intRand >= 30 && intRand < 50) {
				temRand = this.robotParams.getRandRange3();
			} else if (intRand >= 50 && intRand < 70) {
				temRand = this.robotParams.getRandRange4();
			} else if (intRand >= 70 && intRand < 90) {
				temRand = this.robotParams.getRandRange5();
			} else {
				temRand = this.robotParams.getRandRange6();
			}
			list.add(temRand);
		}
		return list;
	}

	protected void buildMiddleOrder(String direction, int count, BigDecimal lastestPrice) {

		List<Double> temRands = getTemRand(count);
		for (int i = 0; i < count; i++) {
			double temRand = getAmount(i, temRands);
			BigDecimal amount = this.scaleAmount(temRand);
			BigDecimal price = generateRandomPrice(lastestPrice);

			if (price.compareTo(BigDecimal.ZERO) == 0 || price.compareTo(BigDecimal.ZERO) < 0) {
				break;
			}
			ExchangeOrderBean orderBean = new ExchangeOrderBean();
			orderBean.setSymbol(this.plateSymbol);
			orderBean.setPrice(price);
			orderBean.setAmount(amount);
			orderBean.setDirection(direction);
			orderBean.setType(this.exchangeTYPE);
			orderBean.setUid(this.uid);
			orderBean.setUseDiscount(0);
			orderBean.setSign(this.sign);

			this.sendOrder(orderBean);

			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				logger.error("Robot exception：buildMiddleOrder: " + e);
			}
		}
	}

	protected void buildMarketOrder(String direction, int count) {
		List<Double> temRands = getTemRand(count);
		for (int i = 0; i < count; i++) {
			double temRand = getAmount(i, temRands);
			BigDecimal amount = this.scaleAmount(temRand);

			ExchangeOrderBean orderBean = new ExchangeOrderBean();
			orderBean.setSymbol(this.plateSymbol);
			orderBean.setPrice(BigDecimal.ZERO);
			orderBean.setAmount(amount);
			orderBean.setDirection(direction);
			orderBean.setType(this.exchangeTYPEMarket);
			orderBean.setUid(this.uid);
			orderBean.setUseDiscount(0);
			orderBean.setSign(this.sign);

			this.sendOrder(orderBean);

			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				logger.error("Robot exception: buildMarketOrder: " + e);

			}
		}
	}

	protected BigDecimal scaleAmount(double temRand) {
		BigDecimal val = BigDecimal.ZERO;
		while (val.compareTo(BigDecimal.ZERO) == 0) {
			val = BigDecimal.valueOf(rand.nextDouble() * temRand + robotParams.getStartAmount())
					.setScale(rand.nextInt(robotParams.getAmountScale()) + 1, BigDecimal.ROUND_HALF_DOWN);
		}
		return val;
	}

	protected void sendOrder(ExchangeOrderBean order) {
		try {
			ExchangeOrderDirection direction = ExchangeOrderDirection.SELL;
			if (order.getDirection().equals("BUY")) {
				direction = ExchangeOrderDirection.BUY;
			}
			ExchangeOrderType type = ExchangeOrderType.LIMIT_PRICE;
			if ("MARKET_PRICE".equals(order.getType())) {
				type = ExchangeOrderType.MARKET_PRICE;
			}
			exchangeOrderFeign.addOrderMock(Long.valueOf(order.getUid()), order.getSign(), direction, order.getSymbol(), order.getPrice(), order.getAmount(), type);

			this.lastSendOrderTime = Instant.now();
		} catch (Exception e) {
			logger.error("Robot exception: sendOrder3: " + e);

		}

	}

	@Override
	public void startRobot() {
		this.robotParams.setHalt(false);
		robotParamService.update(robotParams.getCoinName(), robotParams);
		logger.info("[" + this.plateSymbol + "] - Set robot to running state");
	}

	@Override
	public void stopRobot() {
		this.robotParams.setHalt(true);
		robotParamService.update(robotParams.getCoinName(), robotParams);
		logger.info("[" + this.plateSymbol + "] - Set robot to stopped state");
	}

	@Override
	public void run() {
		running.set(true);
		while (running.get()) {
			try {
				if (this.robotParams.isHalt()) {
					sleep(3000);
					continue;
				}
				BigDecimal outerPrice = this.getOuterPrice();
				if (outerPrice.compareTo(BigDecimal.ZERO) == 0)
					continue;

				RobotNormalCleanVo vo = this.processCurrentOrder();
				boolean sendOrderFlag = false;

				if (this.currentPrice == null || this.currentPrice.compareTo(BigDecimal.ZERO) == 0)
					this.currentPrice = outerPrice;
				else if (this.currentPrice.compareTo(outerPrice) == 0) {
					BigDecimal absolutePriceStep = this.robotParams.getAbsolutePriceStep();
					if (absolutePriceStep != null && absolutePriceStep.compareTo(BigDecimal.ZERO) > 0) {
						outerPrice = randomAroundMid(outerPrice, absolutePriceStep);
						this.currentPrice = outerPrice;
					} else {
						outerPrice = generateRandomPrice(outerPrice);
						this.currentPrice = outerPrice;
					}
				}

				if (vo.getMoreThanOrder() == 0) {
					sendOrderFlag = addOrder(sendOrderFlag, outerPrice, vo.getAskList(), vo.getBidList());
					sendOrderFlag = marketOrder(sendOrderFlag, outerPrice);
					if (!sendOrderFlag) {
						int temR = this.rand.nextInt(10);
						String temDir = temR > 5 ? "SELL" : "BUY";
						List<BigDecimal> midPrice = new ArrayList<>();
						midPrice.add(outerPrice);
						List<ExchangeOrderBean> mid_orders = this.buildOrderNew(temDir, midPrice);
						if (mid_orders.size() > 0) {
							this.sendOrder(mid_orders.get(0));
						}
					}
				} else {
					sleep(5000);
				}

			} catch (Exception e) {
				logger.error("Start Robot exception: run1:", e);
				sleep(10000);
			}
		}

		running.set(false);
	}

	private BigDecimal calcTickSize(BigDecimal mid) {
		if (mid.compareTo(new BigDecimal("1")) < 0) return new BigDecimal("0.000001");
		if (mid.compareTo(new BigDecimal("10")) < 0) return new BigDecimal("0.0001");
		if (mid.compareTo(new BigDecimal("100")) < 0) return new BigDecimal("0.001");
		if (mid.compareTo(new BigDecimal("1000")) < 0) return new BigDecimal("0.01");
		return new BigDecimal("0.01");
	}

	private BigDecimal calcSpreadPercent(BigDecimal mid) {
		double raw = 1.0 / Math.sqrt(mid.doubleValue()) / 100.0;
		BigDecimal sp = BigDecimal.valueOf(raw);

		BigDecimal min = new BigDecimal("0.00005"); // 0.005%
		BigDecimal max = new BigDecimal("0.0002");  // 0.02%

		if (sp.compareTo(min) < 0) return min;
		if (sp.compareTo(max) > 0) return max;
		return sp;
	}

	private BigDecimal calcBaseStep(BigDecimal tickSize) {
		return tickSize.multiply(new BigDecimal("1.5"));
	}

	private BigDecimal roundToTick(BigDecimal price, BigDecimal tickSize) {
		return price.divide(tickSize, 0, RoundingMode.DOWN).multiply(tickSize);
	}

	private List<BigDecimal> buildBidBook(List<BigDecimal> exitsBidPrices, BigDecimal midPrice, int levelCount) {
		if (levelCount < 6)
			levelCount = 6;
		BigDecimal tickSize = calcTickSize(midPrice);
		BigDecimal spreadPercent = calcSpreadPercent(midPrice);
		BigDecimal baseStep = calcBaseStep(tickSize);

		BigDecimal halfSpread = midPrice.multiply(spreadPercent);
		BigDecimal startBid = midPrice.subtract(halfSpread);

		List<BigDecimal> bids = new ArrayList<>();
		BigDecimal accumulated = BigDecimal.ZERO;
		int i = 1;
		while (i <= levelCount) {
			BigDecimal step = baseStep.multiply(BigDecimal.valueOf(Math.log(i + 1)));
			accumulated = accumulated.add(step);

			BigDecimal price = startBid.subtract(accumulated);
			price = getScalePrice(roundToTick(price, tickSize));
			if (!isExitsPrice(exitsBidPrices, price)) {
				bids.add(price);
				exitsBidPrices.add(price);
			}
			i++;
		}
		return bids;
	}

	private List<BigDecimal> buildAskBook(List<BigDecimal> exitsAskPrices, BigDecimal midPrice, int levelCount) {
		if (levelCount < 6)
			levelCount = 6;
		BigDecimal tickSize = calcTickSize(midPrice);
		BigDecimal spreadPercent = calcSpreadPercent(midPrice);
		BigDecimal baseStep = calcBaseStep(tickSize);

		BigDecimal halfSpread = midPrice.multiply(spreadPercent);
		BigDecimal startAsk = midPrice.add(halfSpread);

		List<BigDecimal> asks = new ArrayList<>();
		BigDecimal accumulated = BigDecimal.ZERO;
		int i = 1;
		while (i <= levelCount) {
			BigDecimal step = baseStep.multiply(BigDecimal.valueOf(Math.log(i + 1)));
			accumulated = accumulated.add(step);

			BigDecimal price = startAsk.add(accumulated);
			price = getScalePrice(roundToTick(price, tickSize));
			if (!this.isExitsPrice(exitsAskPrices, price)) {
				asks.add(price);
				exitsAskPrices.add(price);
			}
			i++;
		}
		return asks;
	}

	private List<ExchangeOrderBean> buildOrderNew(String direction, List<BigDecimal> arrPrices) {
		int count = arrPrices.size();
		List<ExchangeOrderBean> list_orders = new ArrayList<>();
		List<Double> temRands = getTemRand(count);
		for (int i = 0; i < count; i++) {
			double temRand = getAmount(i, temRands);
			BigDecimal amount = this.scaleAmount(temRand);
			BigDecimal price = arrPrices.get(i);
			price = price.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);

			ExchangeOrderBean orderBean = new ExchangeOrderBean();
			orderBean.setSymbol(this.plateSymbol);
			orderBean.setPrice(price);
			orderBean.setAmount(amount);
			orderBean.setDirection(direction);
			orderBean.setType(this.exchangeTYPE);
			orderBean.setUid(this.uid);
			orderBean.setUseDiscount(0);
			orderBean.setSign(this.sign);
			list_orders.add(orderBean);
		}
		return list_orders;
	}

	private Map<String, JSONObject> getPlateResult(String symbol) {
		Map<String, JSONObject> plateResult = monitorFeign.traderPlateMini(symbol);
		int count = 0;
		while (plateResult == null) {
			try {
				count += 1;
				if (count > 5)
					break;
				sleep(500);
				plateResult = monitorFeign.traderPlateMini(symbol);
			} catch (Exception e) {
				break;
			}
		}
		return plateResult;
	}

	private boolean addOrder(boolean sendOrderFlag, BigDecimal lastestPrice, List<BigDecimal> exitsAskPrices, List<BigDecimal> exitsBidPrices) {
		try {
			if (lastestPrice != null && lastestPrice.compareTo(BigDecimal.ZERO) != 0) {
				Map<String, JSONObject> plateResult = getPlateResult(this.plateSymbol);
				if (plateResult != null && !plateResult.isEmpty()) {
					BigDecimal absolutePriceStep = this.robotParams.getAbsolutePriceStep();
					BigDecimal askLowestPrice = plateResult.get("ask").getBigDecimal("lowestPrice"); // giá bán thấp nhất
					JSONArray askList = plateResult.get("ask").getJSONArray("items");

					BigDecimal bidHighestPrice = plateResult.get("bid").getBigDecimal("highestPrice"); // giá mua cao nhất
					JSONArray bidList = plateResult.get("bid").getJSONArray("items");

					List<BigDecimal> addAskPrices = new ArrayList<>();
					List<BigDecimal> addBidPrices = new ArrayList<>();
					if (absolutePriceStep != null && absolutePriceStep.compareTo(BigDecimal.ZERO) > 0) {
						int countAsk = Math.min(this.robotParams.getInitOrderCount(), askList.size());
						for (int i = 0; i < countAsk; i++) {
							JSONObject json = getJsonObject(askList.get(i));
							BigDecimal price = this.getScalePrice(json.getBigDecimal("price"));
							if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
								exitsAskPrices.add(price);
							}
						}
						addAskPrices = this.buildAskBook(exitsAskPrices, lastestPrice, this.robotParams.getInitOrderCount() - exitsAskPrices.size());

						int countBid = Math.min(this.robotParams.getInitOrderCount(), bidList.size());
						for (int i = 0; i < countBid; i++) {
							JSONObject json = getJsonObject(bidList.get(i));
							BigDecimal price = this.getScalePrice(json.getBigDecimal("price"));
							if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
								exitsBidPrices.add(price);
							}
						}
						addBidPrices = this.buildBidBook(exitsBidPrices, lastestPrice, this.robotParams.getInitOrderCount() - exitsBidPrices.size());
					}

					List<ExchangeOrderBean> buy_orders = new ArrayList<>();
					List<ExchangeOrderBean> sell_orders = new ArrayList<>();
					if (askList.size() == 0 && bidList.size() == 0) {
						this.buildInitPlateOrder(this.robotParams.getInitOrderCount(), lastestPrice);
					} else {
						if (addAskPrices.size() > 0) {
							sell_orders = this.buildOrderNew("SELL", addAskPrices);
							sendOrderFlag = true;
						}
						if (addBidPrices.size() > 0) {
							buy_orders = this.buildOrderNew("BUY", addBidPrices);
							sendOrderFlag = true;
						}
						if (absolutePriceStep != null && absolutePriceStep.compareTo(BigDecimal.ZERO) > 0) {
							if (askList.size() == 0 && sell_orders.isEmpty()) {
								sell_orders = this.buildOrder("SELL", this.robotParams.getInitOrderCount(), lastestPrice, true);
								sendOrderFlag = true;
							}
							if (bidList.size() == 0 && buy_orders.isEmpty()) {
								buy_orders = this.buildOrder("BUY", this.robotParams.getInitOrderCount(), lastestPrice, true);
								sendOrderFlag = true;
							}
						}

						if (absolutePriceStep == null || absolutePriceStep.compareTo(BigDecimal.ZERO) == 0) {
							if (askList.size() < this.robotParams.getInitOrderCount()) {
								if (askList.size() == 0) {
									sell_orders = this.buildOrder("SELL", this.robotParams.getInitOrderCount() - askList.size(), lastestPrice, true);
								} else {
									sell_orders = this.buildOrder("SELL", this.robotParams.getInitOrderCount() - askList.size(), lastestPrice, false);
								}
								sendOrderFlag = true;
							} else {
								if (askLowestPrice.subtract(lastestPrice).compareTo(this.robotParams.getMaxSubPrice()) > 0) {
									sell_orders = this.buildOrder("SELL", 3, lastestPrice, false);
									sendOrderFlag = true;
								}
							}
							if (bidList.size() < this.robotParams.getInitOrderCount()) {
								if (bidList.size() == 0) {
									buy_orders = this.buildOrder("BUY", this.robotParams.getInitOrderCount() - bidList.size(), lastestPrice, true);
								} else {
									buy_orders = this.buildOrder("BUY", this.robotParams.getInitOrderCount() - bidList.size(), lastestPrice, false);
								}
								sendOrderFlag = true;
							} else {
								if (lastestPrice.subtract(bidHighestPrice).compareTo(this.robotParams.getMaxSubPrice()) > 0) {
									buy_orders = this.buildOrder("BUY", 3, lastestPrice, false);
									sendOrderFlag = true;
								}
							}

							if (bidList.size() > 0 && askList.size() > 0) {
								if (askLowestPrice.subtract(bidHighestPrice).compareTo(this.robotParams.getMaxSubPrice()) > 0) {
									sell_orders = this.buildOrder("SELL", 3, lastestPrice, false);
									buy_orders = this.buildOrder("BUY", 3, lastestPrice, false);
									sendOrderFlag = true;
								}
							}
						}
					}

					int buy_count = buy_orders.size();
					int sell_count = sell_orders.size();
					if (buy_count > 0 || sell_count > 0) {
						sendOrderFlag = true;
						if (buy_count >= sell_count) {
							for (int i = 0; i < buy_count; i++) {
								this.sendOrder(buy_orders.get(i));
								sleep(100);
								if (i < sell_count) {
									this.sendOrder(sell_orders.get(i));
									sleep(100);
								}
							}
						} else {
							for (int i = 0; i < sell_count; i++) {
								this.sendOrder(sell_orders.get(i));
								sleep(100);
								if (i < buy_count) {
									this.sendOrder(buy_orders.get(i));
									sleep(100);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Robot exception: run1: {}", e.getMessage(), e);
		} finally {
			try {
				Thread.sleep(this.rand.nextInt(this.robotParams.getRunTime()) + this.robotParams.getRunTime());
			} catch (InterruptedException e) {
				logger.error("Robot exception: run2: {}", e.getMessage(), e);
			}
		}
		return sendOrderFlag;
	}

	private boolean marketOrder(boolean sendOrderFlag, BigDecimal lastestPrice) {
		try {
			if (lastestPrice != null && lastestPrice.compareTo(BigDecimal.ZERO) > 0) {
				Map<String, JSONObject> plateResult = getPlateResult(this.plateSymbol);
				if (plateResult != null && !plateResult.isEmpty()) {
					BigDecimal askLowestPrice = plateResult.get("ask").getBigDecimal("lowestPrice");
					JSONArray askList = plateResult.get("ask").getJSONArray("items");

					BigDecimal bidHighestPrice = plateResult.get("bid").getBigDecimal("highestPrice");
					JSONArray bidList = plateResult.get("bid").getJSONArray("items");

					if (askList.size() == 0 && bidList.size() == 0) {
						return sendOrderFlag;
					} else {
						if (lastestPrice.compareTo(askLowestPrice) >= 0) {
							BigDecimal totalAmount = BigDecimal.ZERO;
							// int sumCount = 0;
							for (int i = 0; i < askList.size(); i++) {
								JSONObject ask = getJsonObject(askList.get(i));
								if (ask.getBigDecimal("price").compareTo(lastestPrice) <= 0) {
									totalAmount = totalAmount.add(ask.getBigDecimal("amount"));
									// sumCount += 1;
								} else {
									break;
								}
							}

							this.buildSingleOrder("BUY", lastestPrice, totalAmount.setScale(this.robotParams.getAmountScale(), BigDecimal.ROUND_HALF_DOWN));
//							if (askList.size() - sumCount < 15) {
//
//							}
							sendOrderFlag = true;
						}

						if (lastestPrice.compareTo(bidHighestPrice) <= 0) {
							BigDecimal totalAmount = BigDecimal.valueOf(0);
							for (int i = 0; i < bidList.size(); i++) {
								JSONObject bid = getJsonObject(bidList.get(i));
								if (bid.getBigDecimal("price").compareTo(lastestPrice) >= 0) {
									totalAmount = totalAmount.add(bid.getBigDecimal("amount"));
								} else {
									break;
								}
							}

							this.buildSingleOrder("SELL", lastestPrice, totalAmount.setScale(this.robotParams.getAmountScale(), BigDecimal.ROUND_HALF_DOWN));
							sendOrderFlag = true;
						}

					}
				}
			}
		} catch (Exception e) {
			logger.error("Robot exception: run1: {}", e.getMessage(), e);
		} finally {
			try {
				Thread.sleep(this.rand.nextInt(this.robotParams.getRunTime()) + this.robotParams.getRunTime());
			} catch (InterruptedException e) {
				logger.error("Robot exception: run2: {}", e.getMessage(), e);
			}
		}
		return sendOrderFlag;
	}

	@Override
	public BigDecimal getOuterPrice() {
		try {
			String pair = this.plateSymbol.replace("/", "").toLowerCase();
			MessageResult mr = robotMarketFeign.findThumb(pair);
			if (mr.getCode() == 0) {
				JSONObject obj = (JSONObject) JSONObject.toJSON(mr.getData());
				BigDecimal price = obj.getBigDecimal("price");
				return price;
			}

		} catch (IllegalStateException e) {
			logger.error("Robot exception: getOuterPrice1: " + e);
			return BigDecimal.ZERO;
		} catch (Exception e) {
			logger.error("Robot exception: getOuterPrice2: " + e);
			return BigDecimal.ZERO;
		}
		return BigDecimal.ZERO;
	}

	@Override
	public void setRobotParamSevice(RobotParamService service) {
		this.robotParamService = service;
	}

	@Override
	public void setRobotMarketFeign(RobotMarketFeign robotMarketFeign) {
		this.robotMarketFeign = robotMarketFeign;
	}

	@Override
	public void setExchangeOrderFeign(ExchangeOrderFeign exchangeOrderFeign) {
		this.exchangeOrderFeign = exchangeOrderFeign;
	}

	@Override
	public void setMonitorFeign(MonitorFeign monitorFeign) {
		this.monitorFeign = monitorFeign;
	}

	@Override
	public Instant getLastSendOrderTime() {
		return this.lastSendOrderTime;
	}

	@Override
	public void setCustomRobotKlineService(CustomRobotKlineService service) {

	}

	@Override
	public boolean reloadCustomRobotKline() {

		return true;
	}

	@Override
	public void interrupt() {
		running.set(false);
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public void setRobotStrategy(int strategy, String flowPair, BigDecimal flowPercent) {

	}

}

package com.wikex.wikex.robot.normal.robot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.constant.ExchangeOrderType;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.exchange.feign.MonitorFeign;
import com.wikex.wikex.robot.market.feign.RobotMarketFeign;
import com.wikex.wikex.robot.normal.entity.CustomRobotKline;
import com.wikex.wikex.robot.normal.entity.ExchangeOrderBean;
import com.wikex.wikex.robot.normal.entity.RobotParams;
import com.wikex.wikex.robot.normal.service.CustomRobotKlineService;
import com.wikex.wikex.robot.normal.service.RobotParamService;
import com.wikex.wikex.util.MessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExchangeRobotCustom implements Runnable, ExchangeRobot {
	private AtomicBoolean running = new AtomicBoolean(true);

	public Logger logger = LoggerFactory.getLogger(ExchangeRobotCustom.class);

	private RobotMarketFeign robotMarketFeign;

	private ExchangeOrderFeign exchangeOrderFeign;

	private MonitorFeign monitorFeign;

	private RobotParamService robotParamService;

	private CustomRobotKlineService customRobotKlineService;

	protected String plateSymbol;
	protected RobotParams robotParams;

	protected String cDate = "";
	protected int pricePercent = 1;
	protected BigDecimal[] klinePriceArr = new BigDecimal[289];
	protected BigDecimal lastPrice = BigDecimal.ZERO;

	protected int uid = 1;
	protected String sign = "77585211314qazwsx";
	protected String exchangeTYPE = "LIMIT_PRICE";
	protected String directionSELL = "SELL";

	protected Random rand = new Random();

	protected Instant lastSendOrderTime = Instant.now();
	private int lastIndexStart = -1;
	private int tendIndex = 0;
	private int minuteIndex = 0;
	private BigDecimal randPricePercent = BigDecimal.ZERO;

	public String getPlateSymbol() {
		return plateSymbol;
	}

	public void setPlateSymbol(String plateSymbol) {
		this.plateSymbol = plateSymbol;
	}

	public RobotParams getRobotParams() {
		return robotParams;
	}

	public void setRobotParams(RobotParams robotParams) {
		this.robotParams = robotParams;
		this.robotParams.setRobotType(1);
		robotParamService.update(robotParams.getCoinName(), robotParams);
	}

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
		this.robotParams.setRobotType(1);
		this.robotParams.setStrategyType(params.getStrategyType());
		this.robotParams.setFlowPair(params.getFlowPair());
		this.robotParams.setFlowPercent(params.getFlowPercent());

		robotParamService.update(this.plateSymbol, params);
	}

	public void processCurrentOrder() {
		Map<String, String> param = new HashMap<String, String>();
		param.put("symbol", this.plateSymbol);
		param.put("uid", String.valueOf(this.uid));
		param.put("sign", this.sign);
		param.put("pageNo", "0");
		param.put("pageSize", "100");

		try {

			Map<String, JSONObject> plateResult1 = monitorFeign.traderPlateMini((this.plateSymbol));

			while (plateResult1 == null) {
				Thread.sleep(500);
				plateResult1 = monitorFeign.traderPlateMini((this.plateSymbol));
			}
			if (plateResult1 != null && !plateResult1.equals("")) {

				JSONArray askList = plateResult1.get("ask").getJSONArray("items");

				JSONArray bidList = plateResult1.get("bid").getJSONArray("items");

				Page<ExchangeOrder> exchangeOrders = exchangeOrderFeign.currentOrderMock(Long.valueOf(this.uid),
						this.sign, this.plateSymbol, 0, 100);

				while (exchangeOrders == null || exchangeOrders.getRecords() == null) {
					Thread.sleep(500);
					exchangeOrders = exchangeOrderFeign.currentOrderMock(Long.valueOf(this.uid), this.sign,
							this.plateSymbol, 0, 100);
				}

				List<ExchangeOrder> buyOrderList = new ArrayList();
				List<ExchangeOrder> sellOrderList = new ArrayList();
				if (exchangeOrders != null) {
					List<ExchangeOrder> orderList = exchangeOrders.getRecords();
					if (orderList != null && orderList.size() > 0) {
						for (ExchangeOrder order : orderList) {
							if (order.getDirection().getCode() == 0) {
								buyOrderList.add(order);
							} else {
								sellOrderList.add(order);
							}
						}
					}

				}

				if (askList.size() == 0 || bidList.size() == 0) {

					if (buyOrderList.size() > 0) {

						for (int j = 0; j < buyOrderList.size(); j++) {
							this.cancelCurrentOrder(buyOrderList.get(j).getOrderId());
							Thread.sleep(500);
						}
					}
					if (sellOrderList.size() > 0) {

						for (int k = 0; k < sellOrderList.size(); k++) {
							this.cancelCurrentOrder(sellOrderList.get(k).getOrderId());
							Thread.sleep(500);
						}
					}

				} else {

					if (buyOrderList.size() > 24) {

						for (int j = 0; j < buyOrderList.size(); j++) {
							if (j % 3 == 0) {
								this.cancelCurrentOrder(buyOrderList.get(j).getOrderId());
								Thread.sleep(1000);
							}
						}
					}

					if (sellOrderList.size() > 24) {

						for (int k = 0; k < sellOrderList.size(); k++) {
							if (k % 3 == 0) {
								this.cancelCurrentOrder(sellOrderList.get(k).getOrderId());
								Thread.sleep(1000);
							}
						}
					}
				}
			}

		} catch (InterruptedException e) {
			logger.error("Robot exception：processCurrentOrder3: " + e);
		}
	}

	public void cancelCurrentOrder(String orderId) {

		logger.info("Cancel order：" + this.plateSymbol + " - " + orderId);

		MessageResult result = exchangeOrderFeign.cancelOrdermock(Long.valueOf(this.uid), this.sign, orderId);

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
			Thread.sleep(500);
		} catch (InterruptedException e) {
			logger.error("Robot exception：buildSingleOrder: " + e);
		}
	}

	protected void buildInitPlateOrder(int count, BigDecimal lastestPrice) {
		BigDecimal priceBuy = lastestPrice.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
		BigDecimal priceSell = lastestPrice.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
		List<ExchangeOrderBean> buyList = new ArrayList<ExchangeOrderBean>();
		List<ExchangeOrderBean> sellList = new ArrayList<ExchangeOrderBean>();

		for (int i = 0; i < count * 2; i++) {
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

			BigDecimal amount = this.scaleAmount(temRand);

			ExchangeOrderBean orderBean = new ExchangeOrderBean();
			orderBean.setSymbol(this.plateSymbol);

			orderBean.setAmount(amount);

			orderBean.setType(this.exchangeTYPE);
			orderBean.setUid(this.uid);
			orderBean.setUseDiscount(0);
			orderBean.setSign(this.sign);
			if (i % 2 == 0) {
				orderBean.setPrice(priceSell);
				orderBean.setDirection("SELL");

				sellList.add(orderBean);
			} else {
				orderBean.setPrice(priceBuy);
				orderBean.setDirection("BUY");
				buyList.add(orderBean);
			}

			BigDecimal temStep = BigDecimal.valueOf(i + 1)
					.divide(BigDecimal.valueOf(this.robotParams.getInitOrderCount()), 10, BigDecimal.ROUND_HALF_DOWN)
					.multiply(this.robotParams.getPriceStepRate());
			if (i % 2 == 0) {

				priceSell = priceSell.add(priceSell.multiply(this.robotParams.getPriceStepRate().add(temStep)))
						.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
			} else {

				priceBuy = priceBuy.subtract(priceBuy.multiply(this.robotParams.getPriceStepRate().add(temStep)))
						.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
			}

			priceSell = this.scalePrice(priceSell);
			priceBuy = this.scalePrice(priceBuy);

			if (priceBuy.compareTo(BigDecimal.ZERO) <= 0 || priceSell.compareTo(BigDecimal.ZERO) <= 0) {
				break;
			}

		}

		int bIndex = buyList.size();
		int sIndex = sellList.size();
		while (bIndex >= 0 || sIndex >= 0) {
			int randNum = this.rand.nextInt(120);
			if (randNum > 30) {
				bIndex = bIndex - 1;
				if (bIndex >= 0) {
					this.sendOrder(buyList.get(bIndex));
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						logger.error("Robot exception：buildInitPlateOrder1: " + e);
					}
				}
			} else {
				sIndex = sIndex - 1;
				if (sIndex >= 0) {
					this.sendOrder(sellList.get(sIndex));
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						logger.error("Robot exception：buildInitPlateOrder2: " + e);
					}
				}
			}
		}
	}

	protected void buildOrder(String direction, int count, BigDecimal lastestPrice, boolean initFlag) {

		BigDecimal price = lastestPrice.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);

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

			BigDecimal amount = this.scaleAmount(temRand);

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

			BigDecimal temStep = BigDecimal.valueOf(i + 1)
					.divide(BigDecimal.valueOf(this.robotParams.getInitOrderCount()), 10, BigDecimal.ROUND_HALF_DOWN)
					.multiply(this.robotParams.getPriceStepRate());
			if (direction.equals(this.directionSELL)) {

				price = price.add(price.multiply(this.robotParams.getPriceStepRate().add(temStep)))
						.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
			} else {

				price = price.subtract(price.multiply(this.robotParams.getPriceStepRate().add(temStep)))
						.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
			}

			price = this.scalePrice(price);

			if (price.compareTo(BigDecimal.ZERO) == 0 || price.compareTo(BigDecimal.ZERO) < 0) {
				break;
			}
			try {

				if (initFlag) {
					Thread.sleep(200);
				} else {
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {
				logger.error("Robot exception：buildOrder: " + e);
			}
		}
	}

	protected void buildSpecOrder(String direction, BigDecimal startPrice, BigDecimal endPrice) {
		if (startPrice.compareTo(endPrice) > 0) {
			return;
		}
		BigDecimal price = startPrice.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);

		for (int i = 0; i < 3; i++) {
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
			BigDecimal amount = this.scaleAmount(temRand);

			BigDecimal step = BigDecimal.valueOf(i + 1).multiply(BigDecimal.valueOf(0.25));
			price = price.add(endPrice.subtract(startPrice).multiply(step));

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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Robot exception：buildSpecOrder: " + e);
			}
		}
	}

	protected void buildMiddleOrder(String direction, int count, BigDecimal lastestPrice) {

		BigDecimal price = lastestPrice.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);

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
			BigDecimal amount = this.scaleAmount(temRand);

			if (direction.equals(this.directionSELL)) {

				price = price.subtract(price.multiply(this.robotParams.getPriceStepRate()))
						.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
			} else {

				price = price.add(price.multiply(this.robotParams.getPriceStepRate()))
						.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
			}
			price = this.scalePrice(price);
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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Robot exception：buildMiddleOrder: " + e);
			}
		}
	}

	protected BigDecimal scaleAmount(double temRand) {
		BigDecimal randAmount = BigDecimal.valueOf(0);
		while (randAmount.compareTo(BigDecimal.ZERO) == 0) {
			randAmount = BigDecimal.valueOf(this.rand.nextDouble() * temRand + this.robotParams.getStartAmount())
					.setScale(this.rand.nextInt(this.robotParams.getAmountScale()) + 1, BigDecimal.ROUND_HALF_DOWN);
		}
		return randAmount;
	}

	protected BigDecimal scalePrice(BigDecimal price) {
		int temRand = this.rand.nextInt(100);
		if (price.compareTo(BigDecimal.valueOf(1000)) >= 0) {
			if (temRand < 50) {
				return price.setScale(0, BigDecimal.ROUND_HALF_DOWN);
			}

			return price.setScale(this.rand.nextInt(this.robotParams.getScale()), BigDecimal.ROUND_HALF_DOWN);
		}
		if (price.compareTo(BigDecimal.valueOf(100)) >= 0) {
			if (temRand < 50) {
				return price.setScale(1, BigDecimal.ROUND_HALF_DOWN);
			}

			return price.setScale(this.rand.nextInt(this.robotParams.getScale()) + 1, BigDecimal.ROUND_HALF_DOWN);
		}
		if (price.compareTo(BigDecimal.valueOf(10)) >= 0) {
			if (temRand < 50) {
				return price.setScale(2, BigDecimal.ROUND_HALF_DOWN);
			}

			return price.setScale(this.rand.nextInt(this.robotParams.getScale() - 1) + 2, BigDecimal.ROUND_HALF_DOWN);
		}
		if (price.compareTo(BigDecimal.valueOf(1)) >= 0) {
			if (temRand < 50) {
				return price.setScale(3, BigDecimal.ROUND_HALF_DOWN);
			}

			return price.setScale(this.rand.nextInt(this.robotParams.getScale() - 2) + 3, BigDecimal.ROUND_HALF_DOWN);
		}
		if (price.compareTo(BigDecimal.valueOf(0.1)) >= 0) {
			if (temRand < 50) {
				return price.setScale(4, BigDecimal.ROUND_HALF_DOWN);
			}

			return price.setScale(this.rand.nextInt(this.robotParams.getScale() - 3) + 4, BigDecimal.ROUND_HALF_DOWN);
		}
		if (price.compareTo(BigDecimal.valueOf(0.01)) >= 0) {
			if (temRand < 50) {
				return price.setScale(5, BigDecimal.ROUND_HALF_DOWN);
			}
			int temN = this.robotParams.getScale() - 4;
			temN = temN > 0 ? temN : 1;

			return price.setScale(this.rand.nextInt(temN) + 5, BigDecimal.ROUND_HALF_DOWN);
		}
		if (price.compareTo(BigDecimal.valueOf(0.001)) >= 0) {
			if (temRand < 50) {
				return price.setScale(6, BigDecimal.ROUND_HALF_DOWN);
			}
			int temN = this.robotParams.getScale() - 5;
			temN = temN > 0 ? temN : 1;

			return price.setScale(this.rand.nextInt(temN) + 6, BigDecimal.ROUND_HALF_DOWN);
		}
		return price.setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_DOWN);
	}

	protected void sendOrder(ExchangeOrderBean order) {

		BigDecimal price = order.getPrice().setScale(this.robotParams.getScale(), BigDecimal.ROUND_HALF_UP);
		BigDecimal amount = order.getAmount().setScale(this.robotParams.getAmountScale(), BigDecimal.ROUND_HALF_UP)
				.add(BigDecimal.valueOf(1 + rand.nextInt(10)));
		try {

			ExchangeOrderDirection direction = ExchangeOrderDirection.SELL;
			if (order.getDirection().equals("BUY")) {
				direction = ExchangeOrderDirection.BUY;
			}
			ExchangeOrderType type = ExchangeOrderType.LIMIT_PRICE;
			if ("MARKET_PRICE".equals(order.getType())) {
				type = ExchangeOrderType.MARKET_PRICE;
			}
			MessageResult messageResult = exchangeOrderFeign.addOrderMock(Long.valueOf(order.getUid()), order.getSign(),
					direction, order.getSymbol(), price, amount, type);

			this.lastSendOrderTime = Instant.now();

			logger.info("[" + order.getSymbol() + "] " + "======>" + JSON.toJSONString(messageResult));

		} catch (Exception e) {
			logger.error("Robot exception：sendOrder3: " + e);
		}
	}

	public void startRobot() {
		this.robotParams.setHalt(false);
		robotParamService.update(robotParams.getCoinName(), robotParams);
		logger.info("[" + this.plateSymbol + "] - Set robot to START state");
	}

	public void stopRobot() {
		this.robotParams.setHalt(true);
		robotParamService.update(robotParams.getCoinName(), robotParams);
		logger.info("[" + this.plateSymbol + "] - Set robot to STOP state");
	}

	@Override
	public void run() {
		logger.info("[" + this.plateSymbol + "] - Thread started...");
		running.set(true);
		while (running.get()) {
			if (this.robotParams.isHalt()) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					logger.info("ERROR: Robot encountered exception 2");
					logger.error("Robot exception: run1: " + e);
				}
				continue;
			}
			logger.info("I am starting to process the current order, I am: " + this.plateSymbol);
			this.processCurrentOrder();

			Map<String, String> param = new HashMap<String, String>();
			param.put("symbol", this.plateSymbol);
			Map<String, JSONObject> plateResult;
			boolean sendOrderFlag = false;
			try {
				plateResult = monitorFeign.traderPlateMini((this.plateSymbol));

				while (plateResult == null) {
					Thread.sleep(500);
					plateResult = monitorFeign.traderPlateMini((this.plateSymbol));
				}
				if (plateResult != null) {

					BigDecimal askLowestPrice = plateResult.get("ask").getBigDecimal("lowestPrice");
					JSONArray askList = plateResult.get("ask").getJSONArray("items");

					BigDecimal bidHighestPrice = plateResult.get("bid").getBigDecimal("highestPrice");
					JSONArray bidList = plateResult.get("bid").getJSONArray("items");

					BigDecimal lastestPrice = BigDecimal.ZERO;
					lastestPrice = this.getOuterPrice();

					if (lastestPrice != null && lastestPrice.compareTo(BigDecimal.ZERO) != 0) {
						// If no buy or sell orders, initialize plate
						if (askList.size() == 0 && bidList.size() == 0) {
							this.buildInitPlateOrder(this.robotParams.getInitOrderCount(), lastestPrice);
						} else {
							// If ask list is not full, add SELL orders
							if (askList.size() < 26) {
								if (askList.size() == 0) {
									this.buildOrder("SELL", this.robotParams.getInitOrderCount() - askList.size(),
											lastestPrice, true);
								} else {
									this.buildOrder("SELL", this.robotParams.getInitOrderCount() - askList.size(),
											lastestPrice, false);
								}
								sendOrderFlag = true;
							}

							// If bid list is not full, add BUY orders
							if (bidList.size() < 26) {
								if (bidList.size() == 0) {
									this.buildOrder("BUY", this.robotParams.getInitOrderCount() - bidList.size(),
											lastestPrice, true);
								} else {
									this.buildOrder("BUY", this.robotParams.getInitOrderCount() - bidList.size(),
											lastestPrice, false);
								}
								sendOrderFlag = true;
							}

							// If external price is higher than the lowest ask, buy to push price up
							if (lastestPrice.compareTo(askLowestPrice) > 0) {
								BigDecimal totalAmount = BigDecimal.valueOf(0);
								for (int i = 0; i < askList.size(); i++) {
									JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(askList.get(i)));
									if (jsonObject.getBigDecimal("price").compareTo(lastestPrice) < 0) {
										totalAmount = totalAmount.add(jsonObject.getBigDecimal("amount"));
									} else {
										break;
									}
								}
								totalAmount = totalAmount.add(BigDecimal.valueOf(this.robotParams.getStartAmount())
										.multiply(BigDecimal.valueOf(this.rand.nextInt(20) + 1)));
								this.buildSingleOrder("BUY", lastestPrice,
										totalAmount.setScale(this.robotParams.getAmountScale(),
												BigDecimal.ROUND_HALF_DOWN));
								if (lastestPrice.subtract(bidHighestPrice)
										.compareTo(this.robotParams.getMaxSubPrice()) > 0) {
									this.buildSpecOrder("BUY", bidHighestPrice, lastestPrice);
								}
								sendOrderFlag = true;
							}

							// If external price is lower than the highest bid, sell to push price down
							if (lastestPrice.compareTo(bidHighestPrice) < 0) {
								BigDecimal totalAmount = BigDecimal.valueOf(0);
								for (int i = 0; i < bidList.size(); i++) {
									JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(bidList.get(i)));
									if (jsonObject.getBigDecimal("price").compareTo(lastestPrice) > 0) {
										totalAmount = totalAmount.add(jsonObject.getBigDecimal("amount"));
									} else {
										break;
									}
								}
								totalAmount = totalAmount.add(BigDecimal.valueOf(this.robotParams.getStartAmount())
										.multiply(BigDecimal.valueOf(this.rand.nextInt(20) + 1)));
								this.buildSingleOrder("SELL", lastestPrice,
										totalAmount.setScale(this.robotParams.getAmountScale(),
												BigDecimal.ROUND_HALF_DOWN));
								if (askLowestPrice.subtract(lastestPrice)
										.compareTo(this.robotParams.getMaxSubPrice()) > 0) {
									this.buildSpecOrder("SELL", lastestPrice, askLowestPrice);
								}
								sendOrderFlag = true;
							}

							// If spread between bid and ask is too large, add balancing orders
							if (bidList.size() > 0 && askList.size() > 0) {
								if (askLowestPrice.subtract(bidHighestPrice)
										.compareTo(this.robotParams.getMaxSubPrice()) > 0) {
									this.buildOrder("SELL", 3, lastestPrice, false);
									this.buildOrder("BUY", 3, lastestPrice, false);
									sendOrderFlag = true;
								}
							}

							// If no orders were sent, place random middle order
							if (!sendOrderFlag) {
								int temR = this.rand.nextInt(10);
								String temDir = temR > 5 ? "SELL" : "BUY";
								this.buildMiddleOrder(temDir, 1, lastestPrice);
							}
						}
					} else {
						logger.info("Robot error: External price does not exist or is 0");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Robot exception: run2: " + e);
				logger.info("ERROR: Robot encountered exception 1");
			} finally {
				try {
					Thread.sleep(this.rand.nextInt(this.robotParams.getRunTime()) + this.robotParams.getRunTime());
				} catch (InterruptedException e) {
					logger.error("Robot exception: run3: " + e);
					logger.info("ERROR: Robot encountered exception 2");
				}
			}
		}
		running.set(false);
	}

	@Override
	public BigDecimal getOuterPrice() {
		if (this.robotParams.getStrategyType() == 2) {

			Date t = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String dateStr = df.format(t);

			if (!dateStr.equals(this.cDate)) {
				if (!this.reloadCustomRobotKline()) {

					if (this.lastPrice.compareTo(BigDecimal.ZERO) == 0) {
						return BigDecimal.ZERO;
					}
					int noDataRand = rand.nextInt(10);
					int changePercentRand = rand.nextInt(3);
					if (noDataRand > 6) {
						return this.lastPrice.add(this.lastPrice
								.multiply(BigDecimal.valueOf(changePercentRand).divide(BigDecimal.valueOf(100))));
					} else {
						return this.lastPrice.subtract(this.lastPrice
								.multiply(BigDecimal.valueOf(changePercentRand).divide(BigDecimal.valueOf(100))));
					}
				}
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(t);
			int hours = calendar.get(Calendar.HOUR_OF_DAY);
			;
			int minutes = calendar.get(Calendar.MINUTE);
			int seconds = calendar.get(Calendar.SECOND);

			int totalMinutes = hours * 60 + minutes;
			int totalSeconds = hours * 60 * 60 + minutes * 60 + seconds;

			int indexStart = totalMinutes / 5;
			int indexEnd = indexStart + 1;
			int calSeconds = totalSeconds - indexStart * 5 * 60;

			int temMinuteIndex = calSeconds / 60;
			if (this.minuteIndex == 0) {
				this.randPricePercent = BigDecimal.valueOf(this.pricePercent);
			}
			if (this.minuteIndex != temMinuteIndex) {

				this.randPricePercent = BigDecimal.valueOf(rand.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP)
						.multiply(BigDecimal.valueOf(this.pricePercent));
				this.minuteIndex = temMinuteIndex;
			}

			BigDecimal sinValue = BigDecimal.valueOf(Math.PI).divide(BigDecimal.valueOf(2), 8, BigDecimal.ROUND_HALF_UP)
					.multiply(BigDecimal.valueOf(calSeconds).divide(BigDecimal.valueOf(300), 8,
							BigDecimal.ROUND_HALF_UP));

			if (this.lastIndexStart != -1 && this.lastIndexStart != indexStart) {
				this.tendIndex = this.rand.nextInt(5);
			}

			sinValue = this.tendGenerateType(sinValue, this.tendIndex);

			BigDecimal calPrice = this.klinePriceArr[indexStart].subtract(
					(this.klinePriceArr[indexStart].subtract(
							this.klinePriceArr[indexEnd]).divide(
									BigDecimal.valueOf(300), 8, BigDecimal.ROUND_HALF_UP)
							.multiply(
									BigDecimal.valueOf(calSeconds)))
							.multiply(BigDecimal.valueOf(Math.sin(sinValue.doubleValue()))));

			int tendType = this.klinePriceArr[indexStart].compareTo(this.klinePriceArr[indexEnd]) > 0 ? 1 : 0;
			BigDecimal priceRandSeed = BigDecimal.valueOf(rand.nextDouble()).multiply(this.randPricePercent);
			int tem = rand.nextInt(10);

			BigDecimal percentRandValue = priceRandSeed.divide(BigDecimal.valueOf(100), 8, BigDecimal.ROUND_HALF_UP);
			if (tendType == 1) {
				if (rand.nextInt(60) > 55) {
					calPrice = calPrice.add(calPrice.multiply(percentRandValue));
				} else {
					calPrice = calPrice.subtract(calPrice.multiply(percentRandValue));
				}
			} else {
				if (rand.nextInt(60) > 5) {
					calPrice = calPrice.add(calPrice.multiply(percentRandValue));
				} else {
					calPrice = calPrice.subtract(calPrice.multiply(percentRandValue));
				}
			}

			if (calPrice.compareTo(BigDecimal.ZERO) < 0) {
				calPrice = BigDecimal.ZERO;
			}
			this.lastIndexStart = indexStart;
			this.lastPrice = calPrice;
			return calPrice;
		} else if (this.robotParams.getStrategyType() == 1) {
			try {
				String pair = this.robotParams.getFlowPair().replace("/", "").toLowerCase();
				logger.info("getOuterPrice1:" + pair);
				MessageResult mr = robotMarketFeign.findThumb(pair);
				logger.info("getOuterPrice1:" + JSON.toJSONString(mr));
				if (mr.getCode() == 0) {
					JSONObject obj = (JSONObject) JSONObject.toJSON(mr.getData());
					BigDecimal price = obj.getBigDecimal("price");
					return price.multiply(this.robotParams.getFlowPercent()).divide(BigDecimal.valueOf(100), 8,
							RoundingMode.HALF_DOWN);
				}

			} catch (IllegalStateException e) {
				logger.error("Robot exception：getOuterPrice1:" + e);
				return BigDecimal.ZERO;
			} catch (Exception e) {
				logger.error("Robot exception：getOuterPrice2:" + e);
				return BigDecimal.ZERO;
			}
			return BigDecimal.ZERO;
		} else {
			return BigDecimal.ZERO;
		}
	}

	private BigDecimal tendGenerateType(BigDecimal sinValue, int type) {
		BigDecimal pi01 = BigDecimal.valueOf(Math.PI).multiply(BigDecimal.valueOf(0.1));
		BigDecimal pi02 = BigDecimal.valueOf(Math.PI).multiply(BigDecimal.valueOf(0.2));
		BigDecimal pi03 = BigDecimal.valueOf(Math.PI).multiply(BigDecimal.valueOf(0.3));
		BigDecimal pi04 = BigDecimal.valueOf(Math.PI).multiply(BigDecimal.valueOf(0.4));
		BigDecimal pi05 = BigDecimal.valueOf(Math.PI).multiply(BigDecimal.valueOf(0.5));

		if (type == 0) {
			if (sinValue.compareTo(pi01) > 0 && sinValue.compareTo(pi02) < 0) {
				return sinValue.multiply(BigDecimal.valueOf(2));
			}
			if (sinValue.compareTo(pi02) > 0 && sinValue.compareTo(pi04) < 0) {
				return sinValue.multiply(BigDecimal.valueOf(0.6));
			}
		}
		if (type == 1) {
			if (sinValue.compareTo(pi02) > 0 && sinValue.compareTo(pi04) < 0) {
				return sinValue.multiply(BigDecimal.valueOf(0.6));
			}
		}
		if (type == 2) {
			if (sinValue.compareTo(pi01) > 0 && sinValue.compareTo(pi03) < 0) {
				return sinValue.multiply(BigDecimal.valueOf(0.5));
			}
		}
		if (type == 3) {
			if (sinValue.compareTo(pi01) > 0 && sinValue.compareTo(pi02) < 0) {
				return sinValue.multiply(BigDecimal.valueOf(2));
			}
		}
		if (type == 4) {
			if (sinValue.compareTo(pi03) > 0 && sinValue.compareTo(pi04) < 0) {
				return sinValue.multiply(BigDecimal.valueOf(0.7));
			}
		}
		return sinValue;
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
		this.customRobotKlineService = service;
	}

	@Override
	public boolean reloadCustomRobotKline() {
		Date t = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = df.format(t);
		CustomRobotKline kline = this.customRobotKlineService.findOne(this.plateSymbol, dateStr);

		if (kline != null) {
			this.cDate = dateStr;
			this.pricePercent = kline.getPricePencent();

			String jsonStr = kline.getKline();

			JSONArray jsonArr = JSONArray.parseArray(jsonStr);

			for (int i = 0; i < jsonArr.size(); i++) {
				JSONArray arr = jsonArr.getJSONArray(i);
				this.klinePriceArr[arr.getIntValue(0)] = arr.getBigDecimal(1);
			}
			logger.info(dateStr + " K-line data reloaded successfully!");
			return true;
		} else {
			logger.info("Control robot K-line trend data is missing, please add K-line in the backend.");
			return false;
		}
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
		this.robotParams.setStrategyType(strategy);
		this.robotParams.setFlowPair(flowPair);
		this.robotParams.setFlowPercent(flowPercent);

		robotParamService.update(this.plateSymbol, this.robotParams);
	}
}

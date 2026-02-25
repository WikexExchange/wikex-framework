package com.wikex.wikex.admin.controller.exchange;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.config.TradingConfig;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.ExchangeOrderStatus;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.feign.ExchangeCoinFeign;
import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.exchange.feign.MonitorFeign;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.robot.normal.entity.CustomRobotKline;
import com.wikex.wikex.robot.normal.entity.RobotParams;
import com.wikex.wikex.robot.normal.feign.RobotNormalFeign;
import com.wikex.wikex.screen.ExchangeCoinScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.util.Assert.notNull;

@RestController
@RequestMapping("exchange/exchange-coin")
public class ExchangeCoinController extends BaseAdminController {

    private Logger logger = LoggerFactory.getLogger(ExchangeCoinController.class);

    @Value("${spark.system.md5.key}")
    private String md5Key;

    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private MonitorFeign monitorFeign;
    @Autowired
    private MarketFeign marketFeign;

    @Autowired
    private ExchangeCoinFeign exchangeCoinService;

    @Autowired
    private ExchangeOrderFeign exchangeOrderService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private CoinFeign coinService;
    @Autowired
    private RobotNormalFeign robotNormalFeign;
    @Autowired
    private TradingConfig tradingConfig;

    @RequiresPermissions("exchange:exchange-coin:merge")
    @PostMapping("merge")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Add spot trading pair (exchangeCoin)")
    public MessageResult ExchangeCoinList(
            @Valid ExchangeCoin exchangeCoin) {
        logger.info("Add exchange coin: " + JSON.toJSONString(exchangeCoin));

        ExchangeCoin findResult = exchangeCoinService.findBySymbol(exchangeCoin.getSymbol());
        if (findResult != null) {
            return error("[" + exchangeCoin.getSymbol() + "]" + messageSource.getMessage("PAIR_ALREADY_EXISTS"));
        }
        Coin c1 = coinService.findByUnit(exchangeCoin.getBaseSymbol());
        if (c1 == null) {
            return error("[" + exchangeCoin.getBaseSymbol() + "]" + messageSource.getMessage("SETTLEMENT_CURRENCY_NOT_FOUND"));
        }
        Coin c2 = coinService.findByUnit(exchangeCoin.getCoinSymbol());
        if (c2 == null) {
            return error("[" + exchangeCoin.getCoinSymbol() + "]" + messageSource.getMessage("TRADING_CURRENCY_NOT_FOUND"));
        }
        exchangeCoin = exchangeCoinService.save(exchangeCoin);
        return MessageResult.getSuccessInstance(messageSource.getMessage("SUCCESS"), exchangeCoin);
    }

    @RequiresPermissions("exchange:exchange-coin:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Paged query spot trading fee (exchangeCoin)")
    public MessageResult ExchangeCoinList(ExchangeCoinScreen screen) {
        Page<ExchangeCoin> all = exchangeCoinService.findAll(screen);

        // Remote RPC service URL: get the trading pairs supported by the current trading engine
        Map<String, Integer> engineSymbols = monitorFeign.engines();
        for (ExchangeCoin item : all.getRecords()) {
            if (engineSymbols != null && engineSymbols.containsKey(item.getSymbol())) {
                item.setEngineStatus(engineSymbols.get(item.getSymbol())); // 1: running  2: paused
            } else {
                item.setEngineStatus(0); // 0: unavailable
            }
            item.setCurrentTime(Calendar.getInstance().getTimeInMillis());
        }

        Map<String, Integer> marketEngineSymbols = marketFeign.engines();

        for (ExchangeCoin item : all.getRecords()) {
            // Market engine
            if (marketEngineSymbols != null && marketEngineSymbols.containsKey(item.getSymbol())) {
                item.setMarketEngineStatus(marketEngineSymbols.get(item.getSymbol()));
            } else {
                item.setMarketEngineStatus(0);
            }

            // Robot
            if (this.isRobotExists(item)) {
                item.setExEngineStatus(1);
            } else {
                item.setExEngineStatus(0);
            }
        }
        return success(IPage2Page(all));
    }

    /**
     * View trading pair details
     * @param symbol
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Spot pair details (exchangeCoin)")
    public MessageResult detail(
            @RequestParam(value = "symbol") String symbol) {
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        return success(exchangeCoin);
    }

    @RequiresPermissions("exchange:exchange-coin:deletes")
    @PostMapping("deletes")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Delete spot pair (exchangeCoin)")
    public MessageResult deletes(
            @RequestParam(value = "ids") String[] ids) {
        // Check for unfilled orders
        String coins = "";
        for (String id : ids) {
            ExchangeCoin temCoin = exchangeCoinService.findBySymbol(id);
            notNull(temCoin, "ID=" + id + messageSource.getMessage("PAIR_ALREADY_EXISTS"));
            List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(temCoin.getSymbol());
            if (orders.size() > 0) {
                return error(temCoin.getSymbol() + messageSource.getMessage("PAIR_STILL_HAS") + orders.size() + messageSource.getMessage("UNEXECUTED_ORDERS_CANCEL_BEFORE_DELETE"));
            }
            if (temCoin.getEnable() == 1 || temCoin.getExchangeable() == 1) {
                return error(messageSource.getMessage("PLEASE_COLSE") + temCoin.getSymbol() + messageSource.getMessage("TRADING_ENGINE_AND_SET_PAIR_STATUS"));
            }
            coins += temCoin.getSymbol() + ",";
        }
        logger.info("Delete exchange coin: " + coins.substring(0, coins.length() - 1));
        exchangeCoinService.deletes(ids);
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("exchange:exchange-coin:alter-rate")
    @PostMapping("alter-rate")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Modify spot trading pair (exchangeCoin)")
    public MessageResult alterExchangeCoinRate(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "fee", required = false) BigDecimal fee,
            @RequestParam(value = "maxBuyPrice", required = false) BigDecimal maxBuyPrice,
            @RequestParam(value = "minTurnover", required = false) BigDecimal minTurnover,
            @RequestParam(value = "enable", required = false) Integer enable, // enable/disable (1: enable, 2)
            @RequestParam(value = "visible", required = false) Integer visible, // visible (1: yes, 2)
            @RequestParam(value = "exchangeable", required = false) Integer exchangeable, // tradable (1: yes, 2)
            @RequestParam(value = "enableMarketBuy", required = false) Integer enableMarketBuy, // market buy (1: yes, 0: no)
            @RequestParam(value = "enableMarketSell", required = false) Integer enableMarketSell, // market sell (1: yes, 0: no)
            @RequestParam(value = "enableBuy", required = false) Integer enableBuy, // limit buy (1: yes, 0: no)
            @RequestParam(value = "enableSell", required = false) Integer enableSell, // limit sell (1: yes, 0: no)
            @RequestParam(value = "sort", required = false) Integer sort,
            @RequestParam(value = "password") String password,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) throws Exception {
        password = MD5.md5(password + md5Key);
        Assert.isTrue(password.equals(admin.getPassword()), messageSource.getMessage("WRONG_PASSWORD"));
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        if (fee != null) {
            exchangeCoin.setFee(fee); // modify fee
        }
        if (minTurnover != null) {
            exchangeCoin.setMinTurnover(minTurnover);
        }
        if (maxBuyPrice != null) {
            exchangeCoin.setMaxBuyPrice(maxBuyPrice);
        }
        if (sort != null) {
            exchangeCoin.setSort(sort); // set sort
        }
        if (enable != null && enable > 0 && enable < 3) {
            exchangeCoin.setEnable(enable); // enable/disable
        }
        if (visible != null && visible > 0 && visible < 3) {
            exchangeCoin.setVisible(visible);
        }
        if (exchangeable != null && exchangeable > 0 && exchangeable < 3) {
            exchangeCoin.setExchangeable(exchangeable);
        }
        if (enableMarketBuy != null && enableMarketBuy >= 0 && enableMarketBuy < 2) {
            exchangeCoin.setEnableMarketBuy(enableMarketBuy == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        }
        if (enableMarketSell != null && enableMarketSell >= 0 && enableMarketSell < 2) {
            exchangeCoin.setEnableMarketSell(enableMarketSell == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        }
        if (enableBuy != null && enableBuy >= 0 && enableBuy < 2) {
            exchangeCoin.setEnableBuy(enableBuy == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        }
        if (enableSell != null && enableSell >= 0 && enableSell < 2) {
            exchangeCoin.setEnableSell(enableSell == 1 ? BooleanEnum.IS_TRUE.getCode() : BooleanEnum.IS_FALSE.getCode());
        }
        logger.info("Modify exchange coin: " + symbol);
        exchangeCoinService.save(exchangeCoin);
        return success(messageSource.getMessage("SUCCESS"));
    }

    /**
     * Start trading engine (create if not exists)
     * @param symbol
     * @param password
     * @param admin
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:start-trader")
    @PostMapping("start-trader")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Start trading engine")
    public MessageResult startExchangeCoinEngine(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "password") String password,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) throws Exception {
        password = MD5.md5(password + md5Key);
        Assert.isTrue(password.equals(admin.getPassword()), messageSource.getMessage("WRONG_PASSWORD"));
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");

        if (exchangeCoin.getEnable() != 1) {
            return MessageResult.error(500, messageSource.getMessage("PLEASE_ENABLE_PAIR_BEFORE_TRADING"));
        }

        MessageResult result = monitorFeign.startTrader(symbol);

        if (result.getCode() == 0) {
            logger.info("Start exchange engine successful: " + symbol);
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            logger.info("Start exchange engine failed: " + symbol);
            return error(result.getMessage());
        }
    }

    /**
     * Stop trading engine (create if not exists)
     * @param symbol
     * @param password
     * @param admin
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:stop-trader")
    @PostMapping("stop-trader")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Pause trading engine")
    public MessageResult stopExchangeCoinEngine(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "password") String password,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) throws Exception {
        password = MD5.md5(password + md5Key);
        Assert.isTrue(password.equals(admin.getPassword()), messageSource.getMessage("WRONG_PASSWORD"));
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");

        if (exchangeCoin.getExchangeable() != 2) {
            return MessageResult.error(500, messageSource.getMessage("PLEASE_SET_PAIR_NOT_TRADING"));
        }
        MessageResult result = monitorFeign.stopTrader(symbol);
        if (result.getCode() == 0) {
            logger.info("Stop exchange engine successful: " + symbol);
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            logger.info("Stop exchange engine failed: " + symbol);
            return error(result.getMessage());
        }
    }

    /**
     * Reset trading engine
     * @param symbol
     * @param password
     * @param admin
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:reset-trader")
    @PostMapping("reset-trader")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Reset trading engine")
    public MessageResult resetExchangeCoinEngine(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "password") String password,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) throws Exception {
        password = MD5.md5(password + md5Key);
        Assert.isTrue(password.equals(admin.getPassword()), messageSource.getMessage("WRONG_PASSWORD"));
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");

        if (exchangeCoin.getExchangeable() != 1) {
            return MessageResult.error(500, messageSource.getMessage("PLEASE_SET_PAIR_TRADING"));
        }
        MessageResult result = monitorFeign.resetTrader(symbol);

        if (result.getCode() == 0) {
            logger.info("Reset exchange engine successful: " + symbol);
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            logger.info("Reset exchange engine failed: " + symbol);
            return error(result.getMessage());
        }
    }

//    @RequiresPermissions("exchange:exchange-coin:out-excel")
//    @GetMapping("out-excel")
//    @AccessLog(module = AdminModule.EXCHANGE, operation = "Export spot trading fee (exchangeCoin) Excel")
//    public MessageResult outExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        List all = exchangeCoinService.findAll();
//        return new FileUtil().exportExcel(request, response, all, "exchangeCoin");
//    }

    /**
     * Get all base symbol units for trading zones
     *
     * @return
     */
    @PostMapping("all-base-symbol-units")
    public MessageResult getAllBaseSymbolUnits() {
        List<String> list = exchangeCoinService.getBaseSymbol();
        return success(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * Get supported coin symbols for a base symbol (trading zone)
     *
     * @return
     */
    @PostMapping("all-coin-symbol-units")
    public MessageResult getAllCoinSymbolUnits(@RequestParam("baseSymbol") String baseSymbol) {
        List<String> list = exchangeCoinService.getCoinSymbol(baseSymbol);
        return success(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * Cancel all orders under a specific trading pair
     * @param symbol
     * @param password
     * @param admin
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:cancel-all-order")
    @PostMapping("cancel-all-order")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Cancel all orders for a trading pair (exchangeCoin)")
    public MessageResult cancelAllOrderBySymbol(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "password") String password,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) throws Exception {
        password = MD5.md5(password + md5Key);
        Assert.isTrue(password.equals(admin.getPassword()), messageSource.getMessage("WRONG_PASSWORD"));
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        if (exchangeCoin.getExchangeable() != 2) {
            return MessageResult.error(500, messageSource.getMessage("PLEASE_SET_PAIR_NOT_TRADING"));
        }
        List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(symbol);
        List<ExchangeOrder> cancelOrders = new ArrayList<ExchangeOrder>();
        for (ExchangeOrder order : orders) {
            if (order.getStatus() != ExchangeOrderStatus.TRADING) {
                continue;
            }
            if (isExchangeOrderExist(order)) {
                logger.info("Cancel exchange order: (" + symbol + ") " + JSON.toJSONString(orders));
                String serviceName = tradingConfig.getServiceName(order.getSymbol());
                rocketMQTemplate.convertAndSend("exchange-order-cancel-" + serviceName, JSON.toJSONString(order));
                cancelOrders.add(order);
            } else {
                // Force cancel
                exchangeOrderService.forceCancelOrder(order);
            }
        }

        return success(messageSource.getMessage("UNEXECUTED_ORDER_COUNT") + ":" + orders.size() + "," + messageSource.getMessage("SUCCESSFULLY_CANCELED") + ":" + cancelOrders.size(), cancelOrders);
    }

    /**
     * View trading pair order book details (asks, bids, etc.)
     * @param symbol
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:exchange-overview")
    @PostMapping("exchange-overview")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "View pair order book overview")
    public MessageResult overviewExchangeCoin(@RequestParam("symbol") String symbol) {

        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");

        JSONObject result = monitorFeign.traderOverview(symbol);
        logger.info("Overview exchange coin: " + symbol);
        return success(messageSource.getMessage("SUCCESS"), result);
    }

    /**
     * View robot parameters of a trading pair
     * @param symbol
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:robot-config")
    @RequestMapping("robot-config")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "View robot parameters for a pair")
    public MessageResult getRobotConfig(@RequestParam("symbol") String symbol) {

        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        if (exchangeCoin.getRobotType() == 0 || exchangeCoin.getRobotType() == 1) {
            try {
                MessageResult result = robotNormalFeign.getRobotParams(symbol);
                if (result.getCode() == 0) {
                    return success(messageSource.getMessage("SUCCESS"), result.getData());
                } else {
                    return error(messageSource.getMessage("GET_ROBOT_PARAMETERS_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return error(messageSource.getMessage("GET_ROBOT_PARAMETERS_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
            }
        }
//        else if(exchangeCoin.getRobotType() == 1) { // Split out for convenience of control-robot modifications; currently same code
//            String serviceName = "ROBOT-TRADE-NORMAL";
//            String contextPath = "/ernormal";
//            String url = "http://" + serviceName + "/ernormal/getRobotParams?coinName=" + symbol;
//            try {
//                ResponseEntity<JSONObject> resultStr = restTemplate.getForEntity(url, JSONObject.class);
//                logger.info("Get robot config: " + resultStr.toString());
//                JSONObject result = (JSONObject)resultStr.getBody();
//                if(result.getIntValue("code") == 0) {
//                    return success(messageSource.getMessage("SUCCESS"), result.getJSONObject("data"));
//                }else {
//                    return error("Failed to get robot parameters (no robot for this pair or robot unexpectedly stopped)!");
//                }
//            }catch(Exception e) {
//                e.printStackTrace();
//                return error("Failed to get robot parameters (no robot for this pair or robot unexpectedly stopped)!");
//            }
//        }
        else if (exchangeCoin.getRobotType() == 2) {
            // Control robot
            return null;
        } else {
            return null;
        }

    }

    /**
     * Check if a trading robot exists
     * @param coin
     * @return
     */
    private boolean isRobotExists(ExchangeCoin coin) {
        if (coin.getRobotType() == 0 || coin.getRobotType() == 1) {
            try {
                MessageResult result = robotNormalFeign.getRobotParams(coin.getSymbol());
                if (result.getCode() == 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
//        else if(coin.getRobotType() == 1){ // Split out for convenience of control-robot modifications; currently same code
//            String serviceName = "ROBOT-TRADE-NORMAL"; // Control robot is also controlled here
//            String url = "http://" + serviceName + "/ernormal/getRobotParams?coinName=" + coin.getSymbol();
//            try {
//                ResponseEntity<JSONObject> resultStr = restTemplate.getForEntity(url, JSONObject.class);
//                logger.info("Get robot config: " + resultStr.toString());
//                JSONObject result = (JSONObject)resultStr.getBody();
//                if(result.getIntValue("code") == 0) {
//                    return true;
//                }else {
//                    return false;
//                }
//            }catch(Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
        else if (coin.getRobotType() == 2) {
            return false;
        } else {
            return false;
        }
    }

    /**
     * Create/modify robot parameters (general robot)
     * @param symbol
     * @param startAmount
     * @param randRange0
     * @param randRange1
     * @param randRange2
     * @param randRange3
     * @param randRange4
     * @param randRange5
     * @param randRange6
     * @param scale
     * @param amountScale
     * @param maxSubPrice
     * @param initOrderCount
     * @param priceStepRate
     * @param runTime
     * @param admin
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:alter-robot-config")
    @PostMapping("alter-robot-config")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Modify robot parameters for pair")
    public MessageResult alterRobotConfig(
            @RequestParam("symbol") String symbol,
            @RequestParam("isHalt") Integer isHalt,
            @RequestParam("startAmount") Double startAmount,
            @RequestParam("randRange0") Double randRange0,
            @RequestParam("randRange1") Double randRange1,
            @RequestParam("randRange2") Double randRange2,
            @RequestParam("randRange3") Double randRange3,
            @RequestParam("randRange4") Double randRange4,
            @RequestParam("randRange5") Double randRange5,
            @RequestParam("randRange6") Double randRange6,
            @RequestParam("scale") Integer scale,
            @RequestParam("amountScale") Integer amountScale,
            @RequestParam("maxSubPrice") BigDecimal maxSubPrice,
            @RequestParam("initOrderCount") Integer initOrderCount,
            @RequestParam("priceStepRate") BigDecimal priceStepRate,
            @RequestParam("runTime") Integer runTime,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        // General robot and control robot
        if (exchangeCoin.getRobotType() == 0 || exchangeCoin.getRobotType() == 1) {
            RobotParams params = new RobotParams();
            params.setCoinName(symbol);
            if (isHalt.intValue() == 0) {
                params.setHalt(false);
            } else {
                params.setHalt(true);
            }
            params.setStartAmount(startAmount);
            params.setRandRange0(randRange0);
            params.setRandRange1(randRange1);
            params.setRandRange2(randRange2);
            params.setRandRange3(randRange3);
            params.setRandRange4(randRange4);
            params.setRandRange5(randRange5);
            params.setRandRange6(randRange6);
            params.setScale(scale);
            params.setAmountScale(amountScale);
            params.setMaxSubPrice(maxSubPrice);
            params.setInitOrderCount(initOrderCount);
            params.setPriceStepRate(priceStepRate);
            params.setRunTime(runTime);
            params.setRobotType(exchangeCoin.getRobotType());

            // Get control robot strategy
            try {
                MessageResult<RobotParams> result = robotNormalFeign.getRobotParams(symbol);
                if (result.getCode() == 0) {
                    RobotParams robotParams = result.getData();
                    params.setStrategyType(robotParams.getStrategyType());
                    params.setFlowPair(robotParams.getFlowPair());
                    params.setFlowPercent(robotParams.getFlowPercent());
                } else {
                    return error(messageSource.getMessage("GET_ROBOT_PARAMETERS_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return error(messageSource.getMessage("GET_ROBOT_PARAMETERS_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
            }
            try {
                MessageResult messageResult = robotNormalFeign.setRobotParams(params);
                if (messageResult.getCode() == 0) {
                    return success(messageSource.getMessage("SUCCESS"), messageResult);
                } else {
                    return error(messageSource.getMessage("MODIFY_ROBOT_PARAMETERS_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
                }
            } catch (Exception e) {
                return error(messageSource.getMessage("MODIFY_ROBOT_PARAMETERS_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
            }
        } else {
            return error(messageSource.getMessage("MODIFY_ROBOT_PARAMETERS_FAILED_NOT_GENERAL_ROBOT"));
        }
    }

    @RequiresPermissions("exchange:exchange-coin:create-robot-config")
    @PostMapping("create-robot-config")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Create robot parameters for pair")
    public MessageResult createRobotConfig(
            @RequestParam("symbol") String symbol,
            @RequestParam("isHalt") Integer isHalt,
            @RequestParam("startAmount") Double startAmount,
            @RequestParam("randRange0") Double randRange0,
            @RequestParam("randRange1") Double randRange1,
            @RequestParam("randRange2") Double randRange2,
            @RequestParam("randRange3") Double randRange3,
            @RequestParam("randRange4") Double randRange4,
            @RequestParam("randRange5") Double randRange5,
            @RequestParam("randRange6") Double randRange6,
            @RequestParam("scale") Integer scale,
            @RequestParam("amountScale") Integer amountScale,
            @RequestParam("maxSubPrice") BigDecimal maxSubPrice,
            @RequestParam("initOrderCount") Integer initOrderCount,
            @RequestParam("priceStepRate") BigDecimal priceStepRate,
            @RequestParam("runTime") Integer runTime,
            @SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin) {
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        // General robot and control robot
        if (exchangeCoin.getRobotType() == 0 || exchangeCoin.getRobotType() == 1) {
            RobotParams params = new RobotParams();
            params.setCoinName(symbol);
            if (isHalt.intValue() == 0) {
                params.setHalt(false);
            } else {
                params.setHalt(true);
            }
            params.setStartAmount(startAmount);
            params.setRandRange0(randRange0);
            params.setRandRange1(randRange1);
            params.setRandRange2(randRange2);
            params.setRandRange3(randRange3);
            params.setRandRange4(randRange4);
            params.setRandRange5(randRange5);
            params.setRandRange6(randRange6);
            params.setScale(scale);
            params.setAmountScale(amountScale);
            params.setMaxSubPrice(maxSubPrice);
            params.setInitOrderCount(initOrderCount);
            params.setPriceStepRate(priceStepRate);
            params.setRunTime(runTime);
            params.setRobotType(exchangeCoin.getRobotType());
            params.setStrategyType(2); // default: custom
            params.setFlowPair("BTC/USDT"); // default: BTC/USDT
            params.setFlowPercent(BigDecimal.valueOf(1));
            MessageResult result = null;

            try {
                if (exchangeCoin.getRobotType() == 1) {
                    result = robotNormalFeign.createCustomRobot(params);
                } else {
                    result = robotNormalFeign.createRobot(params);
                }
                if (result.getCode() == 0) {
                    return success(messageSource.getMessage("SUCCESS"), result);
                } else {
                    return error(messageSource.getMessage("CREATION_FAILED") + ":" + result.getMessage());
                }
            } catch (Exception e) {
                return error(messageSource.getMessage("CREATE_ROBOT_FAILED_NOT_GENERAL_ROBOT"));
            }
        } else {
            return error(messageSource.getMessage("CREATE_ROBOT_FAILED_NOT_GENERAL_ROBOT"));
        }
    }

    public boolean isExchangeOrderExist(ExchangeOrder order) {
        try {
            ExchangeOrder result = monitorFeign.findOrder(order);
            return result != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Save robot market trend line
     * @param symbol
     * @param kdate
     * @param kline
     * @param pricePencent Allowed price fluctuation range
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:save-robot-kline")
    @PostMapping("save-robot-kline")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Save robot market trend line")
    public MessageResult createRobotKlineData(@RequestParam("symbol") String symbol,
                                              @RequestParam("kdate") String kdate,
                                              @RequestParam("kline") String kline,
                                              @RequestParam("pricePencent") Integer pricePencent) {
        // Check pair
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        if (exchangeCoin.getRobotType() != 1) {
            return error(messageSource.getMessage("PAIR_NOT_GENERAL_ROBOT"));
        }
        if (kdate.equals("") || kdate.length() < 10) {
            return error(messageSource.getMessage("INVALID_DATE_INPUT"));
        }
        kdate = kdate.substring(0, 10); // Front-end format: 2020-12-01T16:00:00.000Z

        // Save
        try {
            MessageResult result = robotNormalFeign.setRobotStrategy(symbol, 2, "BTC/USDT", BigDecimal.ONE);
            if (result.getCode() == 0) {
                // do nothing
            } else {
                return error(messageSource.getMessage("PLEASE_CREATE_ROBOT_FIRST"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return error(messageSource.getMessage("SAVE_FAILED"));
        }

        // Build params
        CustomRobotKline params = new CustomRobotKline();
        params.setCoinName(symbol);
        params.setKdate(kdate);
        params.setKline(kline);
        params.setPricePencent(pricePencent);

//        serviceName = "ROBOT-TRADE-NORMAL";
//        url = "http://" + serviceName + "/ernormal/saveKline";
        try {
//            ResponseEntity<JSONObject> resultStr = restTemplate.postForEntity(url, params, JSONObject.class);
//            logger.info("save robot kline: " + resultStr.toString());
//            JSONObject result = (JSONObject)resultStr.getBody();

            MessageResult result = robotNormalFeign.saveKline(params);

            if (result.getCode() == 0) {
                return success(messageSource.getMessage("SUCCESS"), result);
            } else {
                return error(messageSource.getMessage("CREATION_FAILED") + ":" + result.getMessage());
            }
        } catch (Exception e) {
            return error(messageSource.getMessage("CREATE_ROBOT_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
        }
    }

    /**
     * Set follow-type control trend
     * @param symbol
     * @param pair
     * @param flowPercent
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:save-robot-flow")
    @PostMapping("save-robot-flow")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Set follow-type control trend")
    public MessageResult createRobotFlow(@RequestParam("symbol") String symbol,
                                         @RequestParam("pair") String pair,
                                         @RequestParam("flowPercent") BigDecimal flowPercent) {
        // Check pair
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        if (exchangeCoin.getRobotType() != 1) {
            return error(messageSource.getMessage("PAIR_NOT_GENERAL_ROBOT"));
        }
        if (StringUtils.isEmpty(pair)) {
            return error(messageSource.getMessage("PLEASE_SELECT_FOLLOWING_TRADING_PAIR"));
        }

        try {
            MessageResult result = robotNormalFeign.setRobotStrategy(symbol, 1, pair, flowPercent);
            if (result.getCode() == 0) {
                return success(messageSource.getMessage("SUCCESS"), result.getData());
            } else {
                logger.info("Failed to get robot K-line parameters");
                return error(messageSource.getMessage("GET_ROBOT_KLINE_PARAMETERS_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
            }
        } catch (Exception e) {
            return error(messageSource.getMessage("SAVE_FAILED"));
        }
    }

    /**
     * Get all control-type trading pairs list
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:custom-coin-list")
    @PostMapping("custom-coin-list")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Get all control-type trading pairs list")
    public MessageResult customRobotCoinList() {
        List<ExchangeCoin> coinList = exchangeCoinService.findAllByRobotType(1);
        return success(coinList);
    }

    /**
     * Get control robot K-line trend parameters list (date -> array)
     * @param symbol
     * @param kdate
     * @return
     */
    @RequiresPermissions("exchange:exchange-coin:robot-kline-list")
    @PostMapping("robot-kline-list")
    @AccessLog(module = AdminModule.EXCHANGE, operation = "Get market trend line list")
    public MessageResult RobotKlineDataList(@RequestParam("symbol") String symbol, @RequestParam("kdate") String kdate) {
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        notNull(exchangeCoin, "validate symbol!");
        if (exchangeCoin.getRobotType() != 1) {
            return error(messageSource.getMessage("PAIR_NOT_GENERAL_ROBOT"));
        }

        kdate = kdate.substring(0, 10); // Front-end format: 2020-12-01T16:00:00.000Z

        String currentDate = kdate;
        // By default, get the current date and future dates
        if (currentDate.equals("") || currentDate == null) {
            Date date = new Date();
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            currentDate = sf.format(date);
        }

        try {
            MessageResult result = robotNormalFeign.getRobotKline(symbol, currentDate);
            if (result.getCode() == 0) {
                return success(messageSource.getMessage("SUCCESS"), result.getData());
            } else {
                logger.info("Failed to get robot K-line parameters");
                return error(messageSource.getMessage("GET_ROBOT_KLINE_PARAMETERS_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return error(messageSource.getMessage("GET_ROBOT_KLINE_PARAMETERS_FAILED_NO_ROBOT_OR_ROBOT_STOPPED"));
        }
    }


//    class RobotParams {
//        private String coinName = ""; // e.g. btcusdt
//        private boolean isHalt = true; // paused or not
//        private double startAmount = 0.001; // minimum trading amount
//        private double randRange0 = 20; // random range for amount (1% probability)
//        private double randRange1 = 4; // random range for amount (9% probability)
//        private double randRange2 = 1; // random range (0.0001 ~ 0.09) 20% probability
//        private double randRange3 = 0.1; // random range (0.0001 ~ 0.09) 20% probability
//        private double randRange4 = 0.01; // random range (0.0001 ~ 0.09) 20% probability
//        private double randRange5 = 0.001; // random range (0.0001 ~ 0.09) 20% probability
//        private double randRange6 = 0.0001; // random range (0.0001 ~ 0.09) 10% probability
//        private int scale = 4; // price precision
//        private int amountScale = 6; // amount precision
//        private BigDecimal maxSubPrice = new BigDecimal(20); // max difference between best ask and best bid
//        private int initOrderCount = 30; // initial order count (must be > 24)
//        private BigDecimal priceStepRate = new BigDecimal(0.003); // price step (0.01 = 1%)
//        private int runTime = 1000; // market polling interval (5000 = 5s)
//
//        private int robotType = 0; // robot type
//        private int strategyType = 1; // control-robot strategy (1: follow, 2: custom)
//        private String flowPair = "BTC/USDT"; // follow pair
//        private BigDecimal flowPercent = BigDecimal.valueOf(1); // follow ratio
//
//        public BigDecimal getFlowPercent() {
//            return flowPercent;
//        }
//
//        public void setFlowPercent(BigDecimal flowPercent) {
//            this.flowPercent = flowPercent;
//        }
//
//        public String getFlowPair() { return flowPair; }
//        public void setFlowPair(String flowPair) { this.flowPair = flowPair; }
//
//        public int getStrategyType() { return strategyType; }
//        public void setStrategyType(int strategyType) { this.strategyType = strategyType; }
//
//        public int getRobotType() {
//            return robotType;
//        }
//        public void setRobotType(int robotType) {
//            this.robotType = robotType;
//        }
//
//        public double getStartAmount() {
//            return startAmount;
//        }
//        public void setStartAmount(double startAmount) {
//            this.startAmount = startAmount;
//        }
//        public double getRandRange0() {
//            return randRange0;
//        }
//        public void setRandRange0(double randRange0) {
//            this.randRange0 = randRange0;
//        }
//        public double getRandRange1() {
//            return randRange1;
//        }
//        public void setRandRange1(double randRange1) {
//            this.randRange1 = randRange1;
//        }
//        public double getRandRange2() {
//            return randRange2;
//        }
//        public void setRandRange2(double randRange2) {
//            this.randRange2 = randRange2;
//        }
//        public double getRandRange3() {
//            return randRange3;
//        }
//        public void setRandRange3(double randRange3) {
//            this.randRange3 = randRange3;
//        }
//        public double getRandRange4() {
//            return randRange4;
//        }
//        public void setRandRange4(double randRange4) {
//            this.randRange4 = randRange4;
//        }
//        public double getRandRange5() {
//            return randRange5;
//        }
//        public void setRandRange5(double randRange5) {
//            this.randRange5 = randRange5;
//        }
//        public double getRandRange6() {
//            return randRange6;
//        }
//        public void setRandRange6(double randRange6) {
//            this.randRange6 = randRange6;
//        }
//        public int getScale() {
//            return scale;
//        }
//        public void setScale(int scale) {
//            this.scale = scale;
//        }
//        public int getAmountScale() {
//            return amountScale;
//        }
//        public void setAmountScale(int amountScale) {
//            this.amountScale = amountScale;
//        }
//        public BigDecimal getMaxSubPrice() {
//            return maxSubPrice;
//        }
//        public void setMaxSubPrice(BigDecimal maxSubPrice) {
//            this.maxSubPrice = maxSubPrice;
//        }
//        public int getInitOrderCount() {
//            return initOrderCount;
//        }
//        public void setInitOrderCount(int initOrderCount) {
//            this.initOrderCount = initOrderCount;
//        }
//        public BigDecimal getPriceStepRate() {
//            return priceStepRate;
//        }
//        public void setPriceStepRate(BigDecimal priceStepRate) {
//            this.priceStepRate = priceStepRate;
//        }
//        public int getRunTime() {
//            return runTime;
//        }
//        public void setRunTime(int runTime) {
//            this.runTime = runTime;
//        }
//        public String getCoinName() {
//            return coinName;
//        }
//        public void setCoinName(String coinName) {
//            this.coinName = coinName;
//        }
//        public boolean isHalt() {
//            return isHalt;
//        }
//        public void setHalt(boolean isHalt) {
//            this.isHalt = isHalt;
//        }
//    }


//    class CustomRobotKline{
//        private String coinName = ""; // pair name, e.g. xxxusdt
//        private String kdate = ""; // K-line date, e.g. 2020/02/02
//        private String kline = ""; // K-line array JSON string
//        private int pricePencent = 0; // price fluctuation range
//
//        public int getPricePencent() {
//            return pricePencent;
//        }
//        public void setPricePencent(int pricePencent) {
//            this.pricePencent = pricePencent;
//        }
//        public String getCoinName() {
//            return coinName;
//        }
//        public void setCoinName(String coinName) {
//            this.coinName = coinName;
//        }
//        public String getKdate() {
//            return kdate;
//        }
//        public void setKdate(String kdate) {
//            this.kdate = kdate;
//        }
//        public String getKline() {
//            return kline;
//        }
//        public void setKline(String kline) {
//            this.kline = kline;
//        }
//    }
}

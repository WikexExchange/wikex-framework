package com.wikex.wikex.exchange.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exchange.config.TradingConfig;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.exchange.service.CoinTraderService;
import com.wikex.wikex.exchange.service.ExchangeCoinService;
import com.wikex.wikex.exchange.service.ExchangeOrderDetailService;
import com.wikex.wikex.exchange.service.ExchangeOrderService;
import com.wikex.wikex.exchange.service.ExchangeTradeService;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.service.CoinService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Entrusted Order Processing Class
 */
@Api(tags = "Entrusted Order Processing Class")
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController extends BaseController {
    @Autowired
    private ExchangeOrderService orderService;
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private CoinTraderService coinTraderService;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Value("${exchange.max-cancel-times:-1}")
    private int maxCancelTimes;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ExchangeTradeService exchangeTradeService;
    @Autowired
    private TradingConfig tradingConfig;

    private String addRedisKey = "E_ADD_%s_%s";

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
//        ExchangeOrder order = new ExchangeOrder();
//        System.out.println(JSON.toJSONString(order));
    }

    @ApiOperation(value = "Add Entrusted Order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "direction", value = "Direction 0: Buy  1: Sell"),
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
            @ApiImplicitParam(name = "amount", value = "Amount"),
            @ApiImplicitParam(name = "price", value = "Price"),
            @ApiImplicitParam(name = "type", value = "0 Market Price 1 Limit Price"),
    })
    @PermissionOperation
    @RequestMapping("add")
    public MessageResult addOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            ExchangeOrderDirection direction, String symbol, BigDecimal price,
            BigDecimal amount, ExchangeOrderType type) {

        AuthMember authMember1 = AuthMember.toAuthMember(authMember);
        if (amount == null || direction == null || type == null) {
            return MessageResult.error(500, msService.getMessage("PARAMETER_ERROR"));
        }

        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(addRedisKey, authMember1.getId(), symbol);
        String redisVal = ops.get(key);
        if (redisVal != null) {
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key, "11", 3, TimeUnit.MINUTES);

        Member member = memberFeign.findMemberById(authMember1.getId());

        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE.getCode())) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CANNOT_TRADE"));
        }
        ExchangeOrder order = new ExchangeOrder();

        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NUMBER_OF_ILLEGAL"));
        }

        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        if (exchangeCoin == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }
        if (exchangeCoin.getEnable() != 1 || exchangeCoin.getExchangeable() != 1) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("COIN_FORBIDDEN"));
        }

        if (exchangeCoin.getEnableSell() == BooleanEnum.IS_FALSE.getCode()
                && direction == ExchangeOrderDirection.SELL) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("STOP_SELLING"));
        }

        if (exchangeCoin.getEnableBuy() == BooleanEnum.IS_FALSE.getCode() && direction == ExchangeOrderDirection.BUY) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("STOP_BUYING"));
        }

        // Base coin & trading coin
        String baseCoin = exchangeCoin.getBaseSymbol();
        String exCoin = exchangeCoin.getCoinSymbol();
        Coin coin = null;

        if (direction == ExchangeOrderDirection.SELL) {
            coin = coinService.findByUnit(exCoin);
        } else {
            coin = coinService.findByUnit(baseCoin);
        }
        if (coin == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }

        price = price.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);

        if (direction == ExchangeOrderDirection.BUY && type == ExchangeOrderType.MARKET_PRICE) {
            amount = amount.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
        } else {
            amount = amount.setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_DOWN);

            if (exchangeCoin.getMaxVolume() != null && exchangeCoin.getMaxVolume().compareTo(BigDecimal.ZERO) != 0
                    && exchangeCoin.getMaxVolume().compareTo(amount) < 0) {
                redisTemplate.delete(key);
                return MessageResult
                        .error(msService.getMessage("AMOUNT_OVER_SIZE") + " " + exchangeCoin.getMaxVolume());
            }
            if (exchangeCoin.getMinVolume() != null && exchangeCoin.getMinVolume().compareTo(BigDecimal.ZERO) != 0
                    && exchangeCoin.getMinVolume().compareTo(amount) > 0) {
                redisTemplate.delete(key);
                return MessageResult
                        .error(msService.getMessage("AMOUNT_TOO_SMALL") + " " + exchangeCoin.getMinVolume());
            }
        }

        if (direction == ExchangeOrderDirection.BUY && type == ExchangeOrderType.MARKET_PRICE) {
            if (amount.compareTo(exchangeCoin.getMinTurnover()) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500,
                        msService.getMessage("AMOUNT_TOO_SMALL") + exchangeCoin.getMinTurnover());
            }
        } else if (direction == ExchangeOrderDirection.SELL && type == ExchangeOrderType.MARKET_PRICE) {
            List<ExchangeTrade> latest = exchangeTradeService.findLatest(exchangeCoin.getSymbol(), 1);
            if (latest != null && !latest.isEmpty()) {
                BigDecimal lastPrice = latest.get(0).getPrice().setScale(exchangeCoin.getBaseCoinScale(),
                        BigDecimal.ROUND_DOWN);
                BigDecimal turnover = amount.multiply(lastPrice);
                if (turnover.compareTo(exchangeCoin.getMinTurnover()) < 0) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500,
                            msService.getMessage("AMOUNT_TOO_SMALL") + exchangeCoin.getMinTurnover());
                }
            }
        } else if (type == ExchangeOrderType.LIMIT_PRICE) {
            BigDecimal turnover = amount.multiply(price);
            if (turnover.compareTo(exchangeCoin.getMinTurnover()) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500,
                        msService.getMessage("AMOUNT_TOO_SMALL") + exchangeCoin.getMinTurnover());
            }
        }

        // Wallet checks
        MemberWallet baseCoinWallet = walletService.findByCoinUnitAndMemberId(baseCoin, member.getId());
        MemberWallet exCoinWallet = walletService.findByCoinUnitAndMemberId(exCoin, member.getId());
        if (baseCoinWallet == null || exCoinWallet == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }
        if (baseCoinWallet.getIsLock() == BooleanEnum.IS_TRUE || exCoinWallet.getIsLock() == BooleanEnum.IS_TRUE) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("WALLET_LOCKED"));
        }

        // Check min/max price limits
        if (direction == ExchangeOrderDirection.SELL && exchangeCoin.getMinSellPrice().compareTo(BigDecimal.ZERO) > 0
                && ((price.compareTo(exchangeCoin.getMinSellPrice()) < 0) || type == ExchangeOrderType.MARKET_PRICE)) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("FLOOR_PRICE") + exchangeCoin.getMinSellPrice());
        }
        if (direction == ExchangeOrderDirection.BUY && exchangeCoin.getMaxBuyPrice().compareTo(BigDecimal.ZERO) > 0
                && ((price.compareTo(exchangeCoin.getMaxBuyPrice()) > 0) || type == ExchangeOrderType.MARKET_PRICE)) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("PRICE_CEILING") + exchangeCoin.getMaxBuyPrice());
        }

        // Market price enable check
        if (type == ExchangeOrderType.MARKET_PRICE) {
            if (exchangeCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE.getCode()
                    && direction == ExchangeOrderDirection.BUY) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("NO_MARKET_PRICE_BUY"));
            } else if (exchangeCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE.getCode()
                    && direction == ExchangeOrderDirection.SELL) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("NO_MARKET_PRICE_SELL"));
            }
        }

        // Max order limit
        if (exchangeCoin.getMaxTradingOrder() > 0 && orderService.findCurrentTradingCount(member.getId(), symbol,
                direction) >= exchangeCoin.getMaxTradingOrder()) {
            redisTemplate.delete(key);
            return MessageResult.error(500,
                    msService.getMessage("MAXIMUM_QUANTITY") + exchangeCoin.getMaxTradingOrder());
        }

        long currentTime = Calendar.getInstance().getTimeInMillis();

        // Special publish type QIANGGOU
        if (exchangeCoin.getPublishType() == ExchangeCoinPublishType.QIANGGOU.getCode()) {
            try {
                if (currentTime < dateTimeFormat.parse(exchangeCoin.getStartTime()).getTime()) {
                    if (direction == ExchangeOrderDirection.BUY) {
                        if (currentTime < dateTimeFormat.parse(exchangeCoin.getStartTime()).getTime()) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("ACTIVITY_NOT_STARTED"));
                        }
                    } else {
                        if (member.getId() != 2) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("UNABLE_TO_PLACE_BUY_ORDER"));
                        }
                    }
                } else {
                    if (currentTime < dateTimeFormat.parse(exchangeCoin.getEndTime()).getTime()) {
                        if (direction == ExchangeOrderDirection.SELL) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("UNABLE_TO_PLACE_SELL_ORDER"));
                        }
                        if (type == ExchangeOrderType.MARKET_PRICE) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("ITS_NOT_MARKETABLE"));
                        }
                    } else {
                        if (currentTime < dateTimeFormat.parse(exchangeCoin.getClearTime()).getTime()) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("WINDING_UP"));
                        }
                    }
                }
            } catch (ParseException e) {
                redisTemplate.delete(key);
                // e.printStackTrace();
                return MessageResult.error(500, msService.getMessage("EXAPI_UNKNOWN_ERROR0"));
            }
        }

        // Special publish type FENTAN
        if (exchangeCoin.getPublishType() == ExchangeCoinPublishType.FENTAN.getCode()) {
            try {
                if (currentTime < dateTimeFormat.parse(exchangeCoin.getStartTime()).getTime()) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("ACTIVITY_NOT_STARTED"));
                } else {
                    if (currentTime < dateTimeFormat.parse(exchangeCoin.getEndTime()).getTime()) {
                        if (direction == ExchangeOrderDirection.SELL) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("ACTIVITY_STARTED_CANT_SELL"));
                        } else {
                            if (type == ExchangeOrderType.MARKET_PRICE) {
                                redisTemplate.delete(key);
                                return MessageResult.error(500, msService.getMessage("ITS_NOT_MARKETABLE"));
                            } else {
                                if (price.compareTo(exchangeCoin.getPublishPrice()) != 0) {
                                    redisTemplate.delete(key);
                                    return MessageResult.error(500,
                                            msService.getMessage("ORDER_PRICE") + exchangeCoin.getPublishPrice());
                                }
                            }
                        }
                    } else {
                        if (currentTime < dateTimeFormat.parse(exchangeCoin.getClearTime()).getTime()) {
                            if (member.getId() != 2) {
                                redisTemplate.delete(key);
                                return MessageResult.error(500, msService.getMessage("WINDING_UP"));
                            } else {
                                if (price.compareTo(exchangeCoin.getPublishPrice()) != 0) {
                                    redisTemplate.delete(key);
                                    return MessageResult.error(500,
                                            msService.getMessage("ORDER_PRICE") + exchangeCoin.getPublishPrice());
                                }
                                if (direction == ExchangeOrderDirection.BUY) {
                                    redisTemplate.delete(key);
                                    return MessageResult.error(500, msService.getMessage("PERIOD_LIQUIDATION"));
                                }
                            }
                        }
                    }
                }
            } catch (ParseException e) {
                redisTemplate.delete(key);
                // e.printStackTrace();
                return MessageResult.error(500, msService.getMessage("EXAPI_UNKNOWN_ERROR1"));
            }
        }

        order.setMemberId(member.getId());
        order.setSymbol(symbol);
        order.setBaseSymbol(baseCoin);
        order.setCoinSymbol(exCoin);
        order.setType(type);
        order.setDirection(direction);
        if (order.getType() == ExchangeOrderType.MARKET_PRICE) {
            order.setPrice(BigDecimal.ZERO);
        } else {
            order.setPrice(price);
        }
        order.setUseDiscount("0");
        order.setAmount(amount);

        MessageResult mr = orderService.addOrder(member.getId(), order);
        if (mr.getCode() != 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ORDER_FAILED") + mr.getMessage());
        }

        String serviceName = tradingConfig.getServiceName(order.getSymbol());
        rocketMQTemplate.convertAndSend("exchange-order-" + serviceName, JSON.toJSONString(order));
        MessageResult result = MessageResult.success(msService.getMessage("SWAP_SUCCESS"));
        result.setData(order.getOrderId());
        redisTemplate.delete(key);
        return result;
    }

    // ==== History Orders ====
    @ApiOperation(value = "History Orders")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
    })
    @PermissionOperation
    @RequestMapping("history")
    public org.springframework.data.domain.Page<ExchangeOrder> historyOrder(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String symbol, int pageNo, int pageSize) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        IPage<ExchangeOrder> page = orderService.findHistory(member.getId(), symbol, pageNo, pageSize);
        // long currentTime = Calendar.getInstance().getTimeInMillis();
        // log.info("History Orders: {} at {}", page, currentTime);
        page.getRecords().forEach(exchangeOrder -> {
            BigDecimal fee = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            if (details != null && details.size() > 0) {
                for (ExchangeOrderDetail detail : details) {
                    fee = fee.add(detail.getFee() == null ? BigDecimal.ZERO : detail.getFee());
                }
            }
            exchangeOrder.setDetail(details);
            exchangeOrder.setFee(fee);
        });
        // long endTime = Calendar.getInstance().getTimeInMillis();
        // log.info("History Orders processing time: {} ms", (endTime - currentTime));
        return IPage2Page(page);
    }

    // ==== Personal History Orders ====
    @ApiOperation(value = "Personal Center - History Orders")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
            @ApiImplicitParam(name = "type", value = "0 Market Price 1 Limit Price"),
            @ApiImplicitParam(name = "status", value = "Trading Status"),
            @ApiImplicitParam(name = "startTime", value = "Start Time"),
            @ApiImplicitParam(name = "endTime", value = "End Time"),
            @ApiImplicitParam(name = "direction", value = "Direction 0: Buy  1: Sell"),
    })
    @PermissionOperation
    @RequestMapping("personal/history")
    public org.springframework.data.domain.Page<ExchangeOrder> personalHistoryOrder(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "symbol", required = false) String symbol,
            @RequestParam(value = "type", required = false) ExchangeOrderType type,
            @RequestParam(value = "status", required = false) ExchangeOrderStatus status,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "direction", required = false) ExchangeOrderDirection direction,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        IPage<ExchangeOrder> page = orderService.findPersonalHistory(member.getId(), symbol, type, status, startTime,
                endTime, direction, pageNo, pageSize);
        page.getRecords().forEach(exchangeOrder -> {
            BigDecimal fee = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            if (details != null && details.size() > 0) {
                for (ExchangeOrderDetail detail : details) {
                    fee = fee.add(detail.getFee() == null ? BigDecimal.ZERO : detail.getFee());
                }
            }
            exchangeOrder.setDetail(exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId()));
            exchangeOrder.setFee(fee);
        });
        return IPage2Page(page);
    }

    // ==== Personal Current Orders ====
    @ApiOperation(value = "Personal Center - Current Orders")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
            @ApiImplicitParam(name = "type", value = "0 Market Price 1 Limit Price"),
            @ApiImplicitParam(name = "startTime", value = "Start Time"),
            @ApiImplicitParam(name = "endTime", value = "End Time"),
            @ApiImplicitParam(name = "direction", value = "Direction 0: Buy  1: Sell"),
    })
    @PermissionOperation
    @RequestMapping("personal/current")
    public org.springframework.data.domain.Page<ExchangeOrder> personalCurrentOrder(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "symbol", required = false) String symbol,
            @RequestParam(value = "type", required = false) ExchangeOrderType type,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "direction", required = false) ExchangeOrderDirection direction,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        IPage<ExchangeOrder> page = orderService.findPersonalCurrent(member.getId(), symbol, type, startTime, endTime,
                direction, pageNo, pageSize);
        page.getRecords().forEach(exchangeOrder -> {
            BigDecimal tradedAmount = BigDecimal.ZERO;
            BigDecimal turnover = BigDecimal.ZERO;
            BigDecimal fee = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            exchangeOrder.setDetail(details);
            for (ExchangeOrderDetail trade : details) {
                tradedAmount = tradedAmount.add(trade.getAmount());
                turnover = turnover.add(trade.getTurnover());
                fee = fee.add(trade.getFee() == null ? BigDecimal.ZERO : trade.getFee());
            }
            exchangeOrder.setTradedAmount(tradedAmount);
            exchangeOrder.setTurnover(turnover);
            exchangeOrder.setFee(fee);
        });
        return IPage2Page(page);
    }

    // ==== Current Orders ====
    @ApiOperation(value = "Current Orders")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
    })
    @PermissionOperation
    @RequestMapping("current")
    public org.springframework.data.domain.Page<ExchangeOrder> currentOrder(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String symbol, int pageNo, int pageSize) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Page<ExchangeOrder> page = orderService.findCurrent(member.getId(), symbol, pageNo, pageSize);
        page.getRecords().forEach(exchangeOrder -> {
            BigDecimal tradedAmount = BigDecimal.ZERO;
            BigDecimal turnover = BigDecimal.ZERO;
            BigDecimal fee = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            exchangeOrder.setDetail(details);
            for (ExchangeOrderDetail trade : details) {
                tradedAmount = tradedAmount.add(trade.getAmount());
                turnover = turnover.add(trade.getTurnover());
                fee = fee.add(trade.getFee() == null ? BigDecimal.ZERO : trade.getFee());
            }
            exchangeOrder.setTradedAmount(tradedAmount);
            exchangeOrder.setTurnover(turnover);
            exchangeOrder.setFee(fee);
        });
        return IPage2Page(page);
    }

    // ==== Order Detail ====
    @ApiOperation(value = "Query Entrusted Order Transaction Details")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "Order ID"),
    })
    @PermissionOperation
    @RequestMapping("detail/{orderId}")
    public List<ExchangeOrderDetail> currentOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @PathVariable String orderId) {
        return exchangeOrderDetailService.findAllByOrderId(orderId);
    }

    // ==== Cancel Order ====
    @ApiOperation(value = "Cancel Entrusted Order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "Order ID"),
    })
    @PermissionOperation
    @RequestMapping("cancel/{orderId}")
    public MessageResult cancelOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @PathVariable String orderId) {
        ExchangeOrder order = orderService.getById(orderId);
        AuthMember member = AuthMember.toAuthMember(authMember);
        if (order.getMemberId() != member.getId()) {
            return MessageResult.error(500, msService.getMessage("OPERATION_FORBIDDEN"));
        }
        if (order.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, msService.getMessage("ORDER_STATUS_ERROR"));
        }

        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(order.getSymbol());
        if (exchangeCoin.getPublishType() != ExchangeCoinPublishType.NONE.getCode()) {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            try {
                if (currentTime > dateTimeFormat.parse(exchangeCoin.getEndTime()).getTime() &&
                        currentTime < dateTimeFormat.parse(exchangeCoin.getClearTime()).getTime()) {
                    return MessageResult.error(500, msService.getMessage("CANNOT_CANCEL_ORDER"));
                }
            } catch (ParseException e) {
                // e.printStackTrace();
                return MessageResult.error(500, msService.getMessage("EXAPI_UNKNOWN_ERROR3"));
            }
        }
        if (isExchangeOrderExist(order)) {
            if (maxCancelTimes > 0
                    && orderService.findTodayOrderCancelTimes(member.getId(), order.getSymbol()) >= maxCancelTimes) {
                return MessageResult.error(500,
                        msService.getMessage("CANCELLED") + maxCancelTimes + msService.getMessage("SECOND"));
            }
            String serviceName = tradingConfig.getServiceName(order.getSymbol());
            rocketMQTemplate.convertAndSend("exchange-order-cancel-" + serviceName, JSON.toJSONString(order));
        } else {
            orderService.forceCancelOrder(order);
        }
        return MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
    }

    @ApiOperation(value = "Check if Order Exists in Matching Engine")
    public boolean isExchangeOrderExist(ExchangeOrder order) {
        try {
            ExchangeOrder order1 = coinTraderService.findOrder(order.getSymbol(), order.getOrderId(),
                    order.getType().getCode(), order.getDirection().getCode());
            return order1 != null;
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        }
    }

    @ApiOperation(value = "Get Order Placement Time Limit")
    @GetMapping("/time_limit")
    public MessageResult userAddExchangeTimeLimit() {
        MessageResult mr = new MessageResult();
        mr.setCode(0);
        mr.setMessage("EXAPI_SUCCESS");
        mr.setData(SysConstant.USER_ADD_EXCHANGE_ORDER_TIME_LIMIT_EXPIRE_TIME);
        return mr;
    }
}

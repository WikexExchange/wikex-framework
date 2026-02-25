package com.wikex.wikex.exchange.feign;

import com.alibaba.fastjson.JSON;
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
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.screen.ExchangeOrderScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.service.CoinService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.util.MessageResult;
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

@Slf4j
@RestController
@RequestMapping("/orderFeign")
public class OrderFeignController extends BaseController {
    @Autowired
    private ExchangeOrderService orderService;
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
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private String addRedisKey = "E_ADD_%s_%s";
    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private TradingConfig tradingConfig;

    @Value("${exchange.max-cancel-times:-1}")
    private int maxCancelTimes;

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @GetMapping("/findOne")
    public ExchangeOrder findOne(@RequestParam(value = "id",required = true) String id) throws Exception {
        ExchangeOrder order = orderService.getById(id);
        return order;
    }
    
   @ApiOperation(value = "Process trade matching")
    @PostMapping("/processExchangeTrade")
    public MessageResult processExchangeTrade(@RequestBody ExchangeTrade trade,
                                              @RequestParam(value = "secondReferrerAward",required = true) boolean secondReferrerAward) throws Exception {
        return orderService.processExchangeTrade(trade,secondReferrerAward);
    }

    @PostMapping("/tradeCompleted")
    public MessageResult tradeCompleted(@RequestParam String orderId,
                                        @RequestParam BigDecimal tradedAmount,
                                        @RequestParam BigDecimal turnover){
        return orderService.tradeCompleted(orderId,tradedAmount,turnover);
    }

    @PostMapping("/cancelOrder")
    public MessageResult cancelOrder(@RequestParam("orderId")String orderId,
                                     @RequestParam("tradedAmount")BigDecimal tradedAmount,
                                     @RequestParam("turnover")BigDecimal turnover){
        return orderService.cancelOrder(orderId, tradedAmount, turnover);
    }

    @PostMapping("findAll")
    public Page<ExchangeOrder> findAll(@RequestBody ExchangeOrderScreen screen){
        return orderService.findAll(screen);
    }


    @PostMapping("findAllDetailByOrderId")
    public List<ExchangeOrderDetail> findAllDetailByOrderId(@RequestParam("orderId")String orderId){
        return exchangeOrderDetailService.findAllByOrderId(orderId);
    }

    @PostMapping(value = "findAllTradingOrderBySymbol")
    public List<ExchangeOrder> findAllTradingOrderBySymbol(@RequestParam("symbol")String symbol){
        return orderService.findAllTradingOrderBySymbol(symbol);
    }

    @PostMapping(value = "findByOrderIds")
    public List<ExchangeOrder> findByOrderIds(@RequestBody List<String> orderIds){
        return orderService.findByOrderIds(orderIds);
    }

    @PostMapping(value = "forceCancelOrder")
    public MessageResult forceCancelOrder(@RequestBody ExchangeOrder order){
        orderService.forceCancelOrder(order);
        return MessageResult.success();
    }

    @RequestMapping("mockcurrentydhdnskd")
    public Page<ExchangeOrder> currentOrderMock(
            @RequestParam("uid") Long uid,
            @RequestParam("sign") String sign,
            @RequestParam("symbol") String symbol,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("pageSize") int pageSize) {
        if(uid != 1 && uid != 10001) {
            return null;
        }
        if(!sign.equals("77585211314qazwsx")) {
            return null;
        }
        Page<ExchangeOrder> page = orderService.findCurrent(uid, symbol, pageNo, pageSize);


        return page;
    }

    @RequestMapping("mockovertimeydhdnskd")
    public Page<ExchangeOrder> currentOrderMockOverTime(
            @RequestParam("uid") Long uid,
            @RequestParam("sign") String sign,
            @RequestParam("symbol") String symbol,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("time") Long time){
        if(uid != 1 && uid != 10001) {
            return null;
        }
        if(!sign.equals("77585211314qazwsx")) {
            return null;
        }
        Page<ExchangeOrder> page = orderService.findCurrentOverTime(uid, symbol, pageNo, pageSize, time);


        return page;
    }

    @RequestMapping("mockcancelydhdnskd")
    public MessageResult cancelOrdermock(@RequestParam("uid") Long uid, @RequestParam("sign")String sign, @RequestParam("orderId")String orderId) {
        ExchangeOrder order = orderService.getById(orderId);
        if(uid != 1 && uid != 10001) {
            return MessageResult.error(500, msService.getMessage("OPERATION_FORBIDDEN"));
        }
        if(!sign.equals("77585211314qazwsx")) {
            return MessageResult.error(500, msService.getMessage("OPERATION_FORBIDDEN"));
        }
        if (order.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, msService.getMessage("ORDER_STATUS_ERROR"));
        }
        
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(order.getSymbol());
        if(exchangeCoin.getPublishType() != ExchangeCoinPublishType.NONE.getCode()) {
            long currentTime = Calendar.getInstance().getTimeInMillis(); 
            try {
                
                if(currentTime > dateTimeFormat.parse(exchangeCoin.getEndTime()).getTime() &&
                        currentTime < dateTimeFormat.parse(exchangeCoin.getClearTime()).getTime()) {
                    return MessageResult.error(500, msService.getMessage("CANNOT_CANCEL_ORDER"));
                }
            } catch (ParseException e) {
                // e.printStackTrace();
                return MessageResult.error(500, msService.getMessage("EXAPI_UNKNOWN_ERROR3"));
            }
        }
        if(isExchangeOrderExist(order)){
            
            String serviceName =  tradingConfig.getServiceName(order.getSymbol());
            rocketMQTemplate.convertAndSend("exchange-order-cancel-"+serviceName,JSON.toJSONString(order));
        }
        else{
            
            orderService.forceCancelOrder(order);
        }
        return MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
    }

    
    private boolean isExchangeOrderExist(ExchangeOrder order){
        try {
            ExchangeOrder order1 = coinTraderService.findOrder(order.getSymbol(),order.getOrderId(), order.getType().getCode(), order.getDirection().getCode());
            return order1 != null;
        }
        catch (Exception e){
            // e.printStackTrace();
            return false;
        }
    }

    @RequestMapping("mockaddydhdnskd")
    public MessageResult addOrderMock(
            @RequestParam("uid") Long uid,
            @RequestParam("sign")String sign,
            @RequestParam("direction") ExchangeOrderDirection direction,
            @RequestParam("symbol")String symbol,
            @RequestParam("price")BigDecimal price,
            @RequestParam("amount")BigDecimal amount,
            @RequestParam("type")ExchangeOrderType type) {
        if(direction == null || type == null){
            return MessageResult.error(500,msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        if(uid != 1 && uid != 10001) {
            return MessageResult.error(500,msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        if(!sign.equals("77585211314qazwsx")) {
            return MessageResult.error(500,msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        ExchangeOrder order = new ExchangeOrder();
        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageResult.error(500, msService.getMessage("NUMBER_OF_ILLEGAL"));
        }
        
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(symbol);
        if (exchangeCoin == null) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }
        if(exchangeCoin.getEnable() != 1 || exchangeCoin.getExchangeable() != 1) {
            return MessageResult.error(500, msService.getMessage("COIN_FORBIDDEN"));
        }
        
        String baseCoin = exchangeCoin.getBaseSymbol();
        
        String exCoin = exchangeCoin.getCoinSymbol();
        Coin coin=null;
        
        if (direction == ExchangeOrderDirection.SELL) {
            coin = coinService.findByUnit(exCoin);
        } else {
            coin = coinService.findByUnit(baseCoin);
        }
        if (coin == null) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }
        
        price = price.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
        
        if (direction == ExchangeOrderDirection.BUY && type == ExchangeOrderType.MARKET_PRICE) {
            amount = amount.setScale(exchangeCoin.getBaseCoinScale(), BigDecimal.ROUND_DOWN);
            
            if (amount.compareTo(exchangeCoin.getMinTurnover()) < 0) {
                return MessageResult.error(500, msService.getMessage("MINIMUM_TURNOVER") + exchangeCoin.getMinTurnover());
            }
        } else {
            amount = amount.setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_DOWN);
            
            if(exchangeCoin.getMaxVolume()!=null&&exchangeCoin.getMaxVolume().compareTo(BigDecimal.ZERO)!=0
                    &&exchangeCoin.getMaxVolume().compareTo(amount)<0){
                return MessageResult.error(msService.getMessage("AMOUNT_OVER_SIZE")+" "+exchangeCoin.getMaxVolume());
            }
            if(exchangeCoin.getMinVolume()!=null&&exchangeCoin.getMinVolume().compareTo(BigDecimal.ZERO)!=0
                    &&exchangeCoin.getMinVolume().compareTo(amount)>0){
                return MessageResult.error(msService.getMessage("AMOUNT_TOO_SMALL")+" "+exchangeCoin.getMinVolume());
            }
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return MessageResult.error(500, msService.getMessage("NUMBER_OF_ILLEGAL"));
        }

        if (direction == ExchangeOrderDirection.SELL && exchangeCoin.getMinSellPrice().compareTo(BigDecimal.ZERO) > 0
                && ((price.compareTo(exchangeCoin.getMinSellPrice()) < 0) && type == ExchangeOrderType.LIMIT_PRICE)) {
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }
        
        if(direction == ExchangeOrderDirection.BUY && exchangeCoin.getMaxBuyPrice().compareTo(BigDecimal.ZERO) > 0
                && ((price.compareTo(exchangeCoin.getMaxBuyPrice()) > 0) && type == ExchangeOrderType.LIMIT_PRICE)) {
            return MessageResult.error(500, msService.getMessage("NO_PRICE_CEILING"));
        }
        
        if (type == ExchangeOrderType.MARKET_PRICE) {
            if (exchangeCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE.getCode() && direction == ExchangeOrderDirection.BUY) {
                return MessageResult.error(500, msService.getMessage("NO_MARKET_PRICE_BUY"));
            } else if (exchangeCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE.getCode() && direction == ExchangeOrderDirection.SELL) {
                return MessageResult.error(500, msService.getMessage("NO_MARKET_PRICE_SELL"));
            }
        }

        long currentTime = Calendar.getInstance().getTimeInMillis(); 
        
        if(exchangeCoin.getPublishType() == ExchangeCoinPublishType.QIANGGOU.getCode()) {
            
            try {
                if(currentTime < dateTimeFormat.parse(exchangeCoin.getStartTime()).getTime()) {
                    if(direction == ExchangeOrderDirection.BUY) {
                        
                        if(currentTime < dateTimeFormat.parse(exchangeCoin.getStartTime()).getTime()) {
                            return MessageResult.error(500, msService.getMessage("ACTIVITY_NOT_STARTED"));
                        }
                    }else {
                        
                        if(uid != 2 && uid != 1 && uid != 10001) {
                            return MessageResult.error(500, msService.getMessage("UNABLE_TO_PLACE_BUY_ORDER"));
                        }
                    }
                }else {
                    
                    if(currentTime < dateTimeFormat.parse(exchangeCoin.getEndTime()).getTime()) {
                        if(direction == ExchangeOrderDirection.SELL) {
                            return MessageResult.error(500, msService.getMessage("UNABLE_TO_PLACE_SELL_ORDER"));
                        }
                        if(type == ExchangeOrderType.MARKET_PRICE){
                            return MessageResult.error(500, msService.getMessage("ITS_NOT_MARKETABLE"));
                        }
                    }else {
                        
                        if(currentTime < dateTimeFormat.parse(exchangeCoin.getClearTime()).getTime()) {
                            return MessageResult.error(500, msService.getMessage("WINDING_UP"));
                        }
                    }
                }
            } catch (ParseException e) {
                // e.printStackTrace();
                return MessageResult.error(500,msService.getMessage("EXAPI_UNKNOWN_ERROR0"));
            }
        }
        
        if(exchangeCoin.getPublishType() == ExchangeCoinPublishType.FENTAN.getCode()) {
            try {

                if(currentTime < dateTimeFormat.parse(exchangeCoin.getStartTime()).getTime()) {
                    
                    return MessageResult.error(500, msService.getMessage("ACTIVITY_NOT_STARTED"));
                }else {
                    
                    if(currentTime < dateTimeFormat.parse(exchangeCoin.getEndTime()).getTime()) {
                        if(direction == ExchangeOrderDirection.SELL) {
                            return MessageResult.error(500, msService.getMessage("ACTIVITY_STARTED_CANT_SELL"));
                        }else {
                            if(type == ExchangeOrderType.MARKET_PRICE) {
                                return MessageResult.error(500, msService.getMessage("ITS_NOT_MARKETABLE"));
                            }else {
                                if(price.compareTo(exchangeCoin.getPublishPrice()) != 0) {
                                    return MessageResult.error(500, msService.getMessage("ORDER_PRICE") + exchangeCoin.getPublishPrice());
                                }
                            }
                        }
                    }else {
                        
                        if(currentTime < dateTimeFormat.parse(exchangeCoin.getClearTime()).getTime()) {
                            
                            if(uid != 2 && uid != 1 && uid != 10001) {
                                return MessageResult.error(500, msService.getMessage("WINDING_UP"));
                            }else {
                                if(price.compareTo(exchangeCoin.getPublishPrice()) != 0) {
                                    return MessageResult.error(500, msService.getMessage("ORDER_PRICE") + exchangeCoin.getPublishPrice());
                                }
                                if(direction == ExchangeOrderDirection.BUY) {
                                    return MessageResult.error(500, msService.getMessage("PERIOD_LIQUIDATION"));
                                }
                            }
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return MessageResult.error(500,msService.getMessage("EXAPI_UNKNOWN_ERROR1"));
            }
        }
        order.setMemberId(uid);
        order.setSymbol(symbol);
        order.setBaseSymbol(baseCoin);
        order.setCoinSymbol(exCoin);
        order.setType(type);
        order.setDirection(direction);
        if(order.getType() == ExchangeOrderType.MARKET_PRICE){
            order.setPrice(BigDecimal.ZERO);
        }
        else{
            order.setPrice(price);
        }
        order.setUseDiscount("0");
        
        order.setAmount(amount);

        MessageResult mr = orderService.addOrder(uid, order);
        if (mr.getCode() != 0) {
            return MessageResult.error(500, msService.getMessage("ORDER_FAILED") + mr.getMessage());
        }

        String serviceName =  tradingConfig.getServiceName(order.getSymbol());
        rocketMQTemplate.convertAndSend("exchange-order-"+serviceName,JSON.toJSONString(order));

        MessageResult result = MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
        result.setData(order.getOrderId());
        return result;
    }

    @RequestMapping("getExchangeTurnoverBase")
    public List<ExchangeOrder> getExchangeTurnoverBase(@RequestParam("dateStr")String dateStr){
        return this.orderService.getExchangeTurnoverBase(dateStr);
    }

    @RequestMapping("getExchangeTurnoverCoin")
    public List<ExchangeOrder> getExchangeTurnoverCoin(@RequestParam("dateStr")String dateStr){
        return this.orderService.getExchangeTurnoverCoin(dateStr);
    }

    @RequestMapping("getExchangeTurnoverSymbol")
    public List<ExchangeOrder> getExchangeTurnoverSymbol(@RequestParam("dateStr")String dateStr){
        return this.orderService.getExchangeTurnoverSymbol(dateStr);
    }

    @ApiOperation(value = "Add Entrusted Order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "direction", value = "Direction 0: Buy  1: Sell"),
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
            @ApiImplicitParam(name = "amount", value = "Amount"),
            @ApiImplicitParam(name = "price", value = "Price"),
            @ApiImplicitParam(name = "type", value = "0 Market Price 1 Limit Price"),
    })
    @RequestMapping("addOrder")
    public MessageResult addOrder(@RequestParam("memberId") Long memberId,
                                  @RequestParam("direction")Integer directionCode,
                                  @RequestParam("symbol")String symbol,
                                  @RequestParam("price")BigDecimal price,
                                  @RequestParam("amount")BigDecimal amount,
                                  @RequestParam("type")Integer typeCode) {
        if(amount==null || directionCode == null || typeCode == null){
            return MessageResult.error(500,msService.getMessage("PARAMETER_ERROR"));
        }
        ExchangeOrderDirection direction = ExchangeOrderDirection.creator(directionCode);
        ExchangeOrderType type = ExchangeOrderType.creator(typeCode);
        
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(addRedisKey,memberId,symbol);
        String redisVal = ops.get(key);
        if(redisVal!=null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key,"11",3, TimeUnit.MINUTES);

        Member member = memberFeign.findMemberById(memberId);
        if(member.getTransactionStatus().equals(BooleanEnum.IS_FALSE.getCode())){
            redisTemplate.delete(key);
            return MessageResult.error(500,msService.getMessage("CANNOT_TRADE"));
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
        if(exchangeCoin.getEnable() != 1 || exchangeCoin.getExchangeable() != 1) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("COIN_FORBIDDEN"));
        }
        
        if(exchangeCoin.getEnableSell() == BooleanEnum.IS_FALSE.getCode() && direction == ExchangeOrderDirection.SELL){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("STOP_SELLING"));
        }

        if(exchangeCoin.getEnableBuy() == BooleanEnum.IS_FALSE.getCode() && direction == ExchangeOrderDirection.BUY){
            redisTemplate.delete(key);
            return MessageResult.error(500,  msService.getMessage("STOP_BUYING"));
        }
        
        String baseCoin = exchangeCoin.getBaseSymbol();
        
        String exCoin = exchangeCoin.getCoinSymbol();
        Coin coin=null;
        
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
            
            if (amount.compareTo(exchangeCoin.getMinTurnover()) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("AMOUNT_TOO_SMALL") + exchangeCoin.getMinTurnover());
            }
        } else {
            amount = amount.setScale(exchangeCoin.getCoinScale(), BigDecimal.ROUND_DOWN);
            
            if(exchangeCoin.getMaxVolume()!=null&&exchangeCoin.getMaxVolume().compareTo(BigDecimal.ZERO)!=0
                    &&exchangeCoin.getMaxVolume().compareTo(amount)<0){
                redisTemplate.delete(key);
                return MessageResult.error(msService.getMessage("AMOUNT_OVER_SIZE")+" "+exchangeCoin.getMaxVolume());
            }
            if(exchangeCoin.getMinVolume()!=null&&exchangeCoin.getMinVolume().compareTo(BigDecimal.ZERO)!=0
                    &&exchangeCoin.getMinVolume().compareTo(amount)>0){
                
                redisTemplate.delete(key);
                return MessageResult.error(msService.getMessage("AMOUNT_TOO_SMALL")+" "+exchangeCoin.getMinVolume());
            }
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0 && type == ExchangeOrderType.LIMIT_PRICE) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("EXORBITANT_PRICES"));
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NUMBER_OF_ILLEGAL"));
        }
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
        
        if (direction == ExchangeOrderDirection.SELL && exchangeCoin.getMinSellPrice().compareTo(BigDecimal.ZERO) > 0
                && ((price.compareTo(exchangeCoin.getMinSellPrice()) < 0) || type == ExchangeOrderType.MARKET_PRICE)) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("FLOOR_PRICE") + exchangeCoin.getMinSellPrice());
        }
        
        if(direction == ExchangeOrderDirection.BUY && exchangeCoin.getMaxBuyPrice().compareTo(BigDecimal.ZERO) > 0
                && ((price.compareTo(exchangeCoin.getMaxBuyPrice()) > 0) || type == ExchangeOrderType.MARKET_PRICE)) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("PRICE_CEILING") + exchangeCoin.getMaxBuyPrice());
        }
        
        if (type == ExchangeOrderType.MARKET_PRICE) {
            if (exchangeCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE.getCode() && direction == ExchangeOrderDirection.BUY) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("NO_MARKET_PRICE_BUY"));
            } else if (exchangeCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE.getCode() && direction == ExchangeOrderDirection.SELL) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("NO_MARKET_PRICE_SELL"));
            }
        }
        
        if (exchangeCoin.getMaxTradingOrder() > 0 && orderService.findCurrentTradingCount(member.getId(), symbol, direction) >= exchangeCoin.getMaxTradingOrder()) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("MAXIMUM_QUANTITY") + exchangeCoin.getMaxTradingOrder());
        }

        
        long currentTime = Calendar.getInstance().getTimeInMillis(); 
        
        if(exchangeCoin.getPublishType() == ExchangeCoinPublishType.QIANGGOU.getCode()) {
            
            try {
                if(currentTime < dateTimeFormat.parse(exchangeCoin.getStartTime()).getTime()) {
                    if(direction == ExchangeOrderDirection.BUY) {
                        
                        if(currentTime < dateTimeFormat.parse(exchangeCoin.getStartTime()).getTime()) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("ACTIVITY_NOT_STARTED"));
                        }
                    }else {
                        
                        if(member.getId() != 2) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("UNABLE_TO_PLACE_BUY_ORDER"));
                        }
                    }
                }else {
                    
                    if(currentTime < dateTimeFormat.parse(exchangeCoin.getEndTime()).getTime()) {
                        if(direction == ExchangeOrderDirection.SELL) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("UNABLE_TO_PLACE_SELL_ORDER"));
                        }
                        if(type == ExchangeOrderType.MARKET_PRICE){
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("ITS_NOT_MARKETABLE"));
                        }
                    }else {
                        
                    if(currentTime < dateTimeFormat.parse(exchangeCoin.getClearTime()).getTime()) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("WINDING_UP"));
                        }
                    }
                }
            } catch (ParseException e) {
                redisTemplate.delete(key);
                // e.printStackTrace();
                return MessageResult.error(500,msService.getMessage("EXAPI_UNKNOWN_ERROR0"));
            }
        }
        
        if(exchangeCoin.getPublishType() == ExchangeCoinPublishType.FENTAN.getCode()) {
            try {

                if(currentTime < dateTimeFormat.parse(exchangeCoin.getStartTime()).getTime()) {
                    
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("ACTIVITY_NOT_STARTED"));
                }else {
                    
                    if(currentTime < dateTimeFormat.parse(exchangeCoin.getEndTime()).getTime()) {
                        if(direction == ExchangeOrderDirection.SELL) {
                            redisTemplate.delete(key);
                            return MessageResult.error(500, msService.getMessage("ACTIVITY_STARTED_CANT_SELL"));
                        }else {
                            if(type == ExchangeOrderType.MARKET_PRICE) {
                                redisTemplate.delete(key);
                                return MessageResult.error(500, msService.getMessage("ITS_NOT_MARKETABLE"));
                            }else {
                                if(price.compareTo(exchangeCoin.getPublishPrice()) != 0) {
                                    redisTemplate.delete(key);
                                    return MessageResult.error(500, msService.getMessage("ORDER_PRICE")+exchangeCoin.getPublishPrice());
                                }
                            }
                        }
                    }else {
                        
                        if(currentTime < dateTimeFormat.parse(exchangeCoin.getClearTime()).getTime()) {
                            
                            if(member.getId() != 2 ) {
                                redisTemplate.delete(key);
                                return MessageResult.error(500, msService.getMessage("WINDING_UP"));
                            }else {
                                if(price.compareTo(exchangeCoin.getPublishPrice()) != 0) {
                                    redisTemplate.delete(key);
                                    return MessageResult.error(500, msService.getMessage("ORDER_PRICE")+exchangeCoin.getPublishPrice());
                                }
                                if(direction == ExchangeOrderDirection.BUY) {
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
                return MessageResult.error(500,msService.getMessage("EXAPI_UNKNOWN_ERROR1"));
            }
        }
        order.setMemberId(member.getId());
        order.setSymbol(symbol);
        order.setBaseSymbol(baseCoin);
        order.setCoinSymbol(exCoin);
        order.setType(type);
        order.setDirection(direction);
        if(order.getType() == ExchangeOrderType.MARKET_PRICE){
            order.setPrice(BigDecimal.ZERO);
        }
        else{
            order.setPrice(price);
        }
        order.setUseDiscount("0");
        
        order.setAmount(amount);

        MessageResult mr = orderService.addOrder(member.getId(), order);
        if (mr.getCode() != 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ORDER_FAILED") + mr.getMessage());
        }
        
        
        String serviceName =  tradingConfig.getServiceName(order.getSymbol());
        rocketMQTemplate.convertAndSend("exchange-order-"+serviceName,JSON.toJSONString(order));

        MessageResult result = MessageResult.success(msService.getMessage("SWAP_SUCCESS"));
        result.setData(order.getOrderId());
        redisTemplate.delete(key);
        return result;
    }

    @ApiOperation(value = "Personal Center - Current Orders")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading Pair Symbol"),
            @ApiImplicitParam(name = "type", value = "0 Market Price 1 Limit Price"),
            @ApiImplicitParam(name = "startTime", value = "Start Time"),
            @ApiImplicitParam(name = "endTime", value = "End Time"),
            @ApiImplicitParam(name = "direction", value = "Direction 0: Buy  1: Sell"),

    })
    @RequestMapping("personal/current")
    public Page<ExchangeOrder> personalCurrentOrder(@RequestParam("memberId") Long memberId,
                                                    @RequestParam(value = "symbol",required = false) String symbol,
                                                    @RequestParam(value = "type",required = false) Integer typeCode,
                                                    @RequestParam(value = "startTime",required = false) String startTime,
                                                    @RequestParam(value = "endTime",required = false) String endTime,
                                                    @RequestParam(value = "direction",required = false) Integer directionCode,
                                                    @RequestParam(value = "pageNo",defaultValue = "1") int pageNo,
                                                    @RequestParam(value = "pageSize",defaultValue = "10") int pageSize) {
        ExchangeOrderType type = typeCode!=null ? ExchangeOrderType.creator(typeCode):null;
        ExchangeOrderDirection direction = directionCode!=null ? ExchangeOrderDirection.creator(directionCode):null;
        Page<ExchangeOrder> page = orderService.findPersonalCurrent(memberId, symbol,type,startTime,endTime, direction, pageNo, pageSize);
        page.getRecords().forEach(exchangeOrder -> {
            
            BigDecimal tradedAmount = BigDecimal.ZERO;
            BigDecimal turnover = BigDecimal.ZERO;
            BigDecimal fee = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            exchangeOrder.setDetail(details);
            for (ExchangeOrderDetail trade : details) {
                tradedAmount = tradedAmount.add(trade.getAmount());
                turnover = turnover.add(trade.getTurnover());
                fee=fee.add(trade.getFee()==null?BigDecimal.ZERO:trade.getFee());
            }
            exchangeOrder.setTradedAmount(tradedAmount);
            exchangeOrder.setTurnover(turnover);
            exchangeOrder.setFee(fee);
        });
        return page;
    }

    @ApiOperation(value = "Cancel Order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderId", value = "Order ID"),
    })
    @RequestMapping("cancelOrder4API")
    public MessageResult cancelOrder4API(@RequestParam("memberId") Long memberId,@RequestParam("orderId") String orderId) {
        ExchangeOrder order = orderService.getById(orderId);
        if (!order.getMemberId().equals(memberId)) {
            return MessageResult.error(500, msService.getMessage("OPERATION_FORBIDDEN"));
        }
        if (order.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, msService.getMessage("ORDER_STATUS_ERROR"));
        }
        
        ExchangeCoin exchangeCoin = exchangeCoinService.findBySymbol(order.getSymbol());
        if(exchangeCoin.getPublishType() != ExchangeCoinPublishType.NONE.getCode()) {
            long currentTime = Calendar.getInstance().getTimeInMillis(); 
            try {
                
                if(currentTime > dateTimeFormat.parse(exchangeCoin.getEndTime()).getTime() &&
                        currentTime < dateTimeFormat.parse(exchangeCoin.getClearTime()).getTime()) {
                    return MessageResult.error(500, msService.getMessage("CANNOT_CANCEL_ORDER"));
                }
            } catch (ParseException e) {
                // e.printStackTrace();
                return MessageResult.error(500, msService.getMessage("EXAPI_UNKNOWN_ERROR3"));
            }
        }
        if(isExchangeOrderExist(order)){
            if (maxCancelTimes > 0 && orderService.findTodayOrderCancelTimes(memberId, order.getSymbol()) >= maxCancelTimes) {
                return MessageResult.error(500, msService.getMessage("CANCELLED") + maxCancelTimes + msService.getMessage("SECOND"));
            }
            
            String serviceName =  tradingConfig.getServiceName(order.getSymbol());
            rocketMQTemplate.convertAndSend("exchange-order-cancel-"+serviceName,JSON.toJSONString(order));
        }
        else{
            
            orderService.forceCancelOrder(order);
        }
        return MessageResult.success(msService.getMessage("EXAPI_SUCCESS"));
    }

    @ApiOperation(value = "Personal Center - Historical Orders")
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
    public Page<ExchangeOrder> personalHistoryOrder(
            @RequestParam("memberId") Long memberId,
            @RequestParam(value = "symbol" ,required = false) String symbol,
            @RequestParam(value = "type",required = false) Integer typeCode,
            @RequestParam(value = "status" ,required = false) Integer statusCode,
            @RequestParam(value = "startTime",required = false) String startTime,
            @RequestParam(value = "endTime",required = false) String endTime,
            @RequestParam(value = "direction",required = false) Integer directionCode,
            @RequestParam(value = "pageNo",defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize",defaultValue = "10") int pageSize) {

        ExchangeOrderType type = typeCode!=null ? ExchangeOrderType.creator(typeCode):null;
        ExchangeOrderDirection direction = directionCode!=null ? ExchangeOrderDirection.creator(directionCode):null;
        ExchangeOrderStatus status = statusCode!=null ? ExchangeOrderStatus.creator(statusCode):null;

        Page<ExchangeOrder> page = orderService.findPersonalHistory(memberId, symbol, type, status, startTime, endTime,direction, pageNo, pageSize);

        page.getRecords().forEach(exchangeOrder -> {
            
            BigDecimal fee = BigDecimal.ZERO;
            List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId());
            if(details!=null && details.size()>0){
                for (ExchangeOrderDetail detail : details) {
                    fee=fee.add(detail.getFee()==null?BigDecimal.ZERO:detail.getFee());
                }
            }
            exchangeOrder.setDetail(exchangeOrderDetailService.findAllByOrderId(exchangeOrder.getOrderId()));
            exchangeOrder.setFee(fee);
        });
        return page;
    }

}

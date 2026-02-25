package com.wikex.wikex.swap.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.config.SnowflakeConfig;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.*;
import com.wikex.wikex.swap.service.*;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Entrusted Order Processing Class
 */
@Api(tags = "Entrusted Order Processing Class")
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController extends BaseController {

    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private ContractOrderEntrustService contractOrderEntrustService;
    @Autowired
    private MemberContractPositionService memberContractPositionService;
    @Autowired
    private MemberContractWalletService memberContractWalletService;
    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private MemberTradeLimitService memberTradeLimitService;
    @Autowired
    private SnowflakeConfig snowflakeConfig;
    @Autowired
    private TradingTimesService tradingTimesService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private String openRedisKey = "U_OPEN_%s_%s";
    private String closeRedisKey = "U_CLOSE_%s_%s";

    /**
     * Contract order placement (Open Position) - Gold Standard
     * Two operation types: Buy to open long, Sell to open short
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Contract order placement (Open Position) - Gold Standard. Two operation types: Buy to open long, Sell to open short")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "direction", value = "1: Buy (close short)  2: Sell (close long)"),
            @ApiImplicitParam(name = "type", value = "0: Market 1: Limit 2: Conditional Entrust"),
            @ApiImplicitParam(name = "triggerPrice", value = "Trigger price"),
            @ApiImplicitParam(name = "entrustPrice", value = "Entrust price (If 0 in conditional entrust: execute at market)"),
            @ApiImplicitParam(name = "leverage", value = "Leverage"),
            @ApiImplicitParam(name = "volume", value = "Entrust volume"),
    })
    @PermissionOperation
    @RequestMapping("open")
    @GlobalTransactional(rollbackFor = Exception.class)
    public MessageResult openOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "contractCoinId") Long contractCoinId, // Contract trading pair
            @RequestParam(value = "direction") ContractOrderDirection direction, // 1: Buy (close short) 2: Sell (close
                                                                                 // long)
            @RequestParam(value = "type") ContractOrderType type, // 0: Market 1: Limit 2: Conditional Entrust
            @RequestParam(value = "triggerPrice", required = false) BigDecimal triggerPrice, // Trigger price
            @RequestParam(value = "entrustPrice") BigDecimal entrustPrice, // Entrust price (If 0 in conditional
                                                                           // entrust: execute at market)
            @RequestParam(value = "leverage") BigDecimal leverage, // Leverage
            @RequestParam(value = "volume") BigDecimal volume// Entrust volume
    ) {

        // Input validity check
        if (contractCoinId == null || direction == null || type == null || leverage == null || volume == null) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        if (direction != ContractOrderDirection.BUY && direction != ContractOrderDirection.SELL) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        if (type != ContractOrderType.MARKET_PRICE && type != ContractOrderType.LIMIT_PRICE
                && type != ContractOrderType.SPOT_LIMIT) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        // Check if trading pair exists
        ContractCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }

        if (!tradingTimesService.isTradingTime(contractCoinId)) {
            return MessageResult.error(500, msService.getMessage("NOT_TRADING_TIME"));
        }

        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);

        // Acquire redis lock
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(openRedisKey, user.getId(), contractCoinId);
        String redisVal = ops.get(key);
        if (redisVal != null) {
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key, "11", 3, TimeUnit.MINUTES);// 3 minutes

        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }

        MemberContractWallet memberContractWallet = memberContractWalletService.findByMemberId(member.getId());
        if (memberContractWallet == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("WALLET_NOT_IN"));
        }

        // Check if user wallet exists
        if (direction.equals(ContractOrderDirection.BUY)) {
            memberContractWallet.setUsdtBuyLeverage(leverage);
        } else {
            memberContractWallet.setUsdtSellLeverage(leverage);
        }
        // Save leverage multiplier
        memberContractWalletService.updateById(memberContractWallet);

        // Get leverage multiplier
        // leverage = direction == ContractOrderDirection.BUY ?
        // memberContractWallet.getUsdtBuyLeverage() :
        // memberContractWallet.getUsdtSellLeverage();
        // Limit order and conditional entrust order require entrust price
        if (type == ContractOrderType.LIMIT_PRICE || type == ContractOrderType.SPOT_LIMIT) {
            if (entrustPrice == null) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("INPUT_COMMISSION_PRICE"));
            }
        }

        // Whether user is prohibited from trading
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE)) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CANNOT_TRADE"));
        }

        // Check if trading pair is enabled
        if (contractCoin.getEnable() != 1 || contractCoin.getExchangeable() != 1) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("COIN_FORBIDDEN"));
        }
        // Whether long opening is allowed (buy long)
        if (contractCoin.getEnableOpenBuy() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.BUY) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_LONG1"));
        }
        // Whether short opening is allowed (buy short)
        if (contractCoin.getEnableOpenSell() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.SELL) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_SHORT1"));
        }
        // Whether market buy opening is allowed
        if (contractCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.BUY) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_LONG2"));
        }
        // Whether market sell opening is allowed
        if (contractCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.SELL) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_SHORT2"));
        }

        // Whether leverage multiplier is within allowed range
        if (contractCoin.getLeverageType() == 1) { // Separate multipliers
            String[] leverageArr = contractCoin.getLeverage().split(",");
            boolean isExist = false;
            for (String str : leverageArr) {
                if (BigDecimal.valueOf(Integer.parseInt(str)).compareTo(leverage) == 0) {
                    isExist = true;
                }
            }
            if (!isExist) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_NOT_EXIST"));
            }
        } else { // Range multipliers
            String[] leverageArr = contractCoin.getLeverage().split(",");
            if (leverageArr.length != 2)
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_ERROR"));

            BigDecimal low = BigDecimal.valueOf(Integer.parseInt(leverageArr[0]));
            BigDecimal high = BigDecimal.valueOf(Integer.parseInt(leverageArr[1]));
            if (leverage.compareTo(low) < 0 || leverage.compareTo(high) > 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_NOT_ALLOWED"));
            }
        }
        // Check order volume range

        // Check if contract engine exists
        if (!contractCoinMatchFactory.containsContractCoinMatch(contractCoin.getSymbol())) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_ENGINE_ERROR"));
        }

        // Calculate open price (for slippage > market price)
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(contractCoin.getSymbol()).getNowPrice();
        // Entrust price cannot be too high or too low (limit order must be within 2% of
        // current price)
        if (type == ContractOrderType.LIMIT_PRICE) {
            if (entrustPrice.compareTo(currentPrice.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.2)))) > 0
                    || entrustPrice
                            .compareTo(currentPrice.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(0.2)))) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("ORDER_PRICE_OVERRUN"));
            }
        }

        openPrice = currentPrice;
        // Calculate approximate market execution price
        if (type == ContractOrderType.MARKET_PRICE) {
            if (direction == ContractOrderDirection.BUY) { // Buy, slippage calc, long, higher price execution
                if (contractCoin.getSpreadType() == 1) { // Slippage type: percentage
                    openPrice = currentPrice.add(currentPrice.multiply(contractCoin.getSpread()));
                } else { // Slippage type: fixed amount
                    openPrice = currentPrice.add(contractCoin.getSpread());
                }
            } else { // Sell, slippage calc, short, lower price execution
                if (contractCoin.getSpreadType() == 1) {
                    openPrice = currentPrice.subtract(currentPrice.multiply(contractCoin.getSpread()));
                } else {
                    openPrice = currentPrice.subtract(contractCoin.getSpread());
                }
            }
        }

        if (openPrice.compareTo(BigDecimal.ZERO) <= 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_ENGINE_ERROR"));
        }

        // Open position, check margin sufficiency
        /**
         * This section involves contract margin calculation
         * In open position, need to calculate all positions/orders margin, not limited
         * to this coin
         */
        // 0. Calculate required margin for current open order
        // contract volume * contract face value / leverage (suitable for Gold Standard
        // = USDT margin mode)
        // BigDecimal volume =
        // principalAmount.multiply(leverage).divide(contractCoin.getShareNumber().multiply(openPrice),8,BigDecimal.ROUND_DOWN);
        BigDecimal principalAmount = volume.multiply(openPrice).multiply(contractCoin.getShareNumber()).divide(leverage,
                8, BigDecimal.ROUND_DOWN);
        // 1. Calculate open fee (volume * face value * open fee rate)
        BigDecimal openFee = principalAmount.multiply(leverage).multiply(contractCoin.getOpenFee());

        // If isolated margin mode, just compare available balance
        if (memberContractWallet.getUsdtPattern() == ContractOrderPattern.FIXED) {
            if (principalAmount.add(openFee).compareTo(memberContractWallet.getUsdtBalance()) > 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
            }
        }
        // Cross margin mode, calculate total equity (long + short)
        if (memberContractWallet.getUsdtPattern() == ContractOrderPattern.CROSSED) {
            List<ContractCoin> coins = contractCoinService.list();
            BigDecimal usdtTotalProfitAndLoss = memberContractWalletService.usdtTotalProfitAndLoss(member.getId(),
                    coins);
            if (usdtTotalProfitAndLoss.compareTo(BigDecimal.ZERO) < 0) {
                if (principalAmount.add(openFee)
                        .compareTo(memberContractWallet.getUsdtBalance().add(usdtTotalProfitAndLoss)) > 0) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
                }
            } else {
                if (principalAmount.add(openFee)
                        .compareTo(memberContractWallet.getUsdtBalance().add(usdtTotalProfitAndLoss)) > 0) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
                }
            }
        }

        // Conditional entrust trigger price must be greater than 0
        if (type == ContractOrderType.SPOT_LIMIT) {
            if (triggerPrice.compareTo(BigDecimal.ZERO) <= 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("TRIGGER_PRICE1"));
            }
            if (entrustPrice.compareTo(BigDecimal.ZERO) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("TRIGGER_PRICE2"));
            }
        }

        if (type == ContractOrderType.LIMIT_PRICE || type == ContractOrderType.MARKET_PRICE) {
            MessageResult result = memberContractWalletService.freezeUsdtBalance(memberContractWallet,
                    principalAmount.add(openFee));
            ;
            if (result.getCode() != 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
            }
        } else {
            // Conditional entrust: only one allowed per direction (stop-profit or stop-loss
            // one only)
        }

        // Create new contract entrust order
        ContractOrderEntrust orderEntrust = new ContractOrderEntrust();
        orderEntrust.setContractId(contractCoin.getId()); // Contract ID
        orderEntrust.setMemberId(member.getId()); // User ID
        orderEntrust.setSymbol(contractCoin.getSymbol()); // Trading pair symbol
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); // Base/settlement currency
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); // Coin symbol
        orderEntrust.setDirection(direction); // Open direction: long/short
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));
        orderEntrust.setVolume(volume); // Open volume
        orderEntrust.setTradedVolume(BigDecimal.ZERO); // Traded volume
        orderEntrust.setTradedPrice(BigDecimal.ZERO); // Traded price
        orderEntrust.setPrincipalUnit("USDT"); // Margin unit
        orderEntrust.setPrincipalAmount(principalAmount); // Margin amount
        orderEntrust.setCreateTime(DateUtil.getTimeMillis()); // Open time
        orderEntrust.setLeverage(leverage);
        orderEntrust.setType(type);
        orderEntrust.setTriggerPrice(triggerPrice); // Trigger price
        orderEntrust.setEntrustPrice(entrustPrice); // Entrust price
        orderEntrust.setEntrustType(ContractOrderEntrustType.OPEN); // Open
        orderEntrust.setTriggeringTime(0L); // Trigger time, temporarily unused
        orderEntrust.setShareNumber(contractCoin.getShareNumber());
        orderEntrust.setProfitAndLoss(BigDecimal.ZERO); // PnL (only for closing)
        orderEntrust.setPatterns(memberContractWallet.getUsdtPattern()); // Margin mode
        orderEntrust.setOpenFee(openFee); // Open fee
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_ING); // Entrust status: in entrust
        orderEntrust.setCurrentPrice(openPrice);
        orderEntrust.setIsBlast(0); // Not liquidation order
        if (type == ContractOrderType.SPOT_LIMIT) { // Conditional entrust
            orderEntrust.setIsFromSpot(1);
        } else {
            orderEntrust.setIsFromSpot(0);
        }

        // Save entrust order
        boolean retObj = contractOrderEntrustService.save(orderEntrust);

        if (retObj) {
            // Send message to Exchange system
            rocketMQTemplate.convertAndSend("swap-order-open", JSON.toJSONString(orderEntrust));

            // Notify wallet change
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", memberContractWallet.getId());
            rocketMQTemplate.convertAndSend("member-wallet-change", JSON.toJSONString(jsonObj));

            
            // Return result
            MessageResult result = MessageResult.success(msService.getMessage("SWAP_SUCCESS"));
            result.setData(orderEntrust);
            redisTemplate.delete(key);
            return result;
        } else {
            MessageResult result = MessageResult.error(msService.getMessage("SWAP_FAILED"));
            result.setData(null);
            redisTemplate.delete(key);
            return result;
        }
    }

    /**
     * Contract closing
     * Four operation types: Buy to close short, Sell to close long
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Contract closing - Gold Standard. Four operation types: Buy to close short, Sell to close long")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "direction", value = "1: Buy (close short)  2: Sell (close long)"),
            @ApiImplicitParam(name = "type", value = "0: Market 1: Limit 2: Conditional Entrust"),
            @ApiImplicitParam(name = "triggerPrice", value = "Trigger price"),
            @ApiImplicitParam(name = "entrustPrice", value = "Entrust price (If 0 in conditional entrust: execute at market)"),
            @ApiImplicitParam(name = "volume", value = "Entrust volume"),
    })
    @PermissionOperation
    @RequestMapping("close")
    @GlobalTransactional(rollbackFor = Exception.class)
    public MessageResult closeOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "contractCoinId") Long contractCoinId, // Contract trading pair
            @RequestParam(value = "direction") ContractOrderDirection direction, // 1: Buy (close short) 2: Sell (close
                                                                                 // long)
            @RequestParam(value = "type") ContractOrderType type, // 1: Market 2: Limit 3: Conditional Entrust
            @RequestParam(value = "triggerPrice", required = false) BigDecimal triggerPrice, // Trigger price
            @RequestParam(value = "entrustPrice") BigDecimal entrustPrice, // Entrust price (If 0 in conditional
                                                                           // entrust: execute at market)
            @RequestParam(value = "volume") BigDecimal volume// Entrust volume
    ) {
        // Input validity check
        if (contractCoinId == null || direction == null || type == null || volume == null) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        // Check if contract exists
        ContractCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        // Limit order and conditional entrust require entrust price
        if (type == ContractOrderType.LIMIT_PRICE || type == ContractOrderType.SPOT_LIMIT) {
            if (entrustPrice == null) {
                return MessageResult.error(500, msService.getMessage("INPUT_COMMISSION_PRICE"));
            }
        }
        if (!tradingTimesService.isTradingTime(contractCoinId)) {
            return MessageResult.error(500, msService.getMessage("NOT_TRADING_TIME"));
        }
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);

        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(closeRedisKey, user.getId(), contractCoinId);
        String redisVal = ops.get(key);
        if (redisVal != null) {
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key, "11", 3, TimeUnit.MINUTES);// 3 minutes

        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        // Whether user is prohibited from trading
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE)) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CANNOT_TRADE"));
        }

        // Get account
        MemberContractWallet memberContractWallet = memberContractWalletService.findByMemberId(member.getId());
        if (memberContractWallet == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }

        // Get position info
        QueryWrapper<MemberContractPosition> query = new QueryWrapper<>();
        query.eq("member_id", member.getId());
        query.eq("contract_id", contractCoinId);
        query.eq("direction",
                direction == ContractOrderDirection.BUY ? ContractOrderDirection.SELL : ContractOrderDirection.BUY);

        MemberContractPosition position = memberContractPositionService.getOne(query);
        if (position == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ORDER_DOES_NOT_EXIST"));
        } else if (position != null && !member.getId().equals(position.getMemberId())) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ORDER_DOES_NOT_EXIST"));
        }

        BigDecimal aveAmount = position.getPosition().subtract(position.getFrozenPosition());// Available close volume
        if (aveAmount.compareTo(volume) < 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("WRONG_AMOUNT_COMMISSION"));
        }

        BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(contractCoin.getSymbol()).getNowPrice();

        // Entrust price cannot be too high or too low (limit order must be within 2% of
        // current price)
        if (type == ContractOrderType.LIMIT_PRICE) {
            if (entrustPrice.compareTo(currentPrice.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.02)))) > 0
                    || entrustPrice
                            .compareTo(currentPrice.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(0.02)))) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("ORDER_PRICE_OVERRUN"));
            }
            currentPrice = entrustPrice;
        }
        BigDecimal principalAmount = volume
                .multiply(position.getPrincipalAmount().subtract(position.getFrozenPrincipalAmount()))
                .divide(position.getPosition().subtract(position.getFrozenPosition()), 8, BigDecimal.ROUND_DOWN);

        BigDecimal closeFee = principalAmount.multiply(position.getLeverage()).multiply(contractCoin.getCloseFee());

        // Create new contract entrust order
        ContractOrderEntrust orderEntrust = new ContractOrderEntrust();
        orderEntrust.setContractId(contractCoin.getId()); // Contract ID
        orderEntrust.setMemberId(member.getId()); // User ID
        orderEntrust.setSymbol(contractCoin.getSymbol()); // Trading pair symbol
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); // Base/settlement currency
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); // Coin symbol
        orderEntrust.setDirection(direction); // Close direction: close short/close long
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));
        orderEntrust.setVolume(volume); // Close volume
        orderEntrust.setTradedVolume(BigDecimal.ZERO); // Traded volume
        orderEntrust.setTradedPrice(BigDecimal.ZERO); // Traded price
        orderEntrust.setPrincipalUnit("USDT"); // Margin unit
        orderEntrust.setPrincipalAmount(principalAmount); // Margin amount
        orderEntrust.setCreateTime(DateUtil.getTimeMillis()); // Close time
        orderEntrust.setType(type);
        orderEntrust.setTriggerPrice(triggerPrice); // Trigger price
        orderEntrust.setEntrustPrice(entrustPrice); // Entrust price
        orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE); // Close
        orderEntrust.setTriggeringTime(0L); // Trigger time, unused
        orderEntrust.setShareNumber(contractCoin.getShareNumber());
        orderEntrust.setProfitAndLoss(BigDecimal.ZERO); // PnL (only closing)
        orderEntrust.setPatterns(memberContractWallet.getUsdtPattern()); // Margin mode
        orderEntrust.setCloseFee(closeFee);
        orderEntrust.setCurrentPrice(currentPrice);
        orderEntrust.setIsBlast(0); // Not liquidation order
        if (type == ContractOrderType.SPOT_LIMIT) { // Conditional entrust
            orderEntrust.setIsFromSpot(1);
        } else {
            orderEntrust.setIsFromSpot(0);
        }
        // Calculate how much margin should be deducted when closing (closeVolume /
        // (available+frozen) * total margin)
        orderEntrust.setPrincipalAmount(principalAmount);

        // Calculate slippage deal price (for market order)
        BigDecimal dealPrice = currentPrice;
        if (type == ContractOrderType.MARKET_PRICE) {
            if (direction == ContractOrderDirection.BUY) { // Buy to close short, slippage calc, higher price
                if (contractCoin.getSpreadType() == 1) {
                    dealPrice = currentPrice.add(currentPrice.multiply(contractCoin.getSpread()));
                } else {
                    dealPrice = currentPrice.add(contractCoin.getSpread());
                }
            } else { // Sell to close long, slippage calc, lower price
                if (contractCoin.getSpreadType() == 1) {
                    dealPrice = currentPrice.subtract(currentPrice.multiply(contractCoin.getSpread()));
                } else {
                    dealPrice = currentPrice.subtract(contractCoin.getSpread());
                }
            }
        }
        if (dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_ENGINE_ERROR"));
        }

        position.setFrozenPrincipalAmount(principalAmount.add(position.getFrozenPrincipalAmount()));
        position.setFrozenPosition(volume.add(position.getFrozenPosition()));
        memberContractPositionService.updateById(position);

        // Save entrust order
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_ING); // Entrust status: in entrust
        orderEntrust.setPositionId(position.getId());
        boolean retObj = contractOrderEntrustService.save(orderEntrust);

        if (retObj) {
            // Send message to Exchange system
            rocketMQTemplate.convertAndSend("swap-order-close", JSON.toJSONString(orderEntrust));

            // Notify wallet change
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", memberContractWallet.getId());
            rocketMQTemplate.convertAndSend("member-wallet-change", JSON.toJSONString(jsonObj));

            
            // Return result
            MessageResult result = MessageResult.success(msService.getMessage("SWAP_SUCCESS"));
            result.setData(retObj);
            redisTemplate.delete(key);
            return result;
        } else {
            MessageResult result = MessageResult.error(msService.getMessage("SWAP_FAILED"));
            result.setData(null);
            redisTemplate.delete(key);
            return result;
        }
    }

    /**
     * Contract cancel order
     * 
     * @param authMember
     * @param entrustId
     * @return
     */
    @ApiOperation(value = "Contract cancel order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entrustId", value = "Entrust ID"),
    })
    @PermissionOperation
    @RequestMapping("cancel")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long entrustId) {
        ContractOrderEntrust entrustOrder = contractOrderEntrustService.getById(entrustId);
        if (entrustOrder == null) {
            return MessageResult.error(500, msService.getMessage("ORDER_NO_EXIST"));
        }
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null)
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        if (!entrustOrder.getMemberId().equals(member.getId())) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_OPERATION"));
        }
        if (entrustOrder.getStatus() != ContractOrderEntrustStatus.ENTRUST_ING) {
            return MessageResult.error(500, msService.getMessage("DELEGATE_STATUS_ERROR"));
        }

        // Send message to Exchange system
        rocketMQTemplate.convertAndSend("swap-order-cancel", JSON.toJSONString(entrustOrder));

        
        // Return result
        MessageResult result = MessageResult.success(msService.getMessage("CANCELLATION_SUCCESSFUL"));
        result.setData(entrustOrder);
        return result;
    }

    /**
     * Contract cancel order (cancel all entrusts: limit + conditional + market)
     * 
     * @param authMember
     * @param contractCoinId
     * @return
     */
    @ApiOperation(value = "Contract cancel order (cancel all entrusts: limit + conditional + market)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("cancel-all")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelAllOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null)
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        List<ContractOrderEntrust> orderList = contractOrderEntrustService.list(new QueryWrapper<ContractOrderEntrust>()
                .eq("member_id", member.getId()).eq("contract_id", contractCoinId));
        if (orderList != null && orderList.size() > 0) {
            for (int i = 0; i < orderList.size(); i++) {
                ContractOrderEntrust entrustOrder = orderList.get(i);
                if (!entrustOrder.getMemberId().equals(member.getId())) {
                    continue;
                }
                if (entrustOrder.getStatus() != ContractOrderEntrustStatus.ENTRUST_ING) {
                    continue;
                }

                // Send message to Exchange system
                rocketMQTemplate.convertAndSend("swap-order-cancel", JSON.toJSONString(entrustOrder));
            }
        }
        
        // Return result
        MessageResult result = MessageResult.success(msService.getMessage("CANCELLATION_SUCCESSFUL"));
        return result;
    }

    /**
     * Get current position list
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Get current position list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("position-list")
    public MessageResult positionList(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null)
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        List<MemberContractPosition> positions = memberContractPositionService.queryAllHoldingPositions(member.getId());
        // Get all contract current prices
        Map<String, BigDecimal> priceMap = contractCoinMatchFactory.getMatchMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getNowPrice()));
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("positions", positions);
        resultMap.put("priceMap", priceMap);
        MessageResult result = MessageResult.success("success");
        result.setData(resultMap);
        return result;
    }

    /**
     * Get current position detail
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Get current position detail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("position-detail")
    public MessageResult positionDetail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null)
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        ContractCoin coin = contractCoinService.getById(contractCoinId);
        if (coin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWallet wallet = memberContractWalletService.findByMemberId(member.getId());
        if (wallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }

        // Calculate account equity
        List<ContractCoin> coins = contractCoinService.list();
        BigDecimal usdtTotalProfitAndLoss = memberContractWalletService.usdtTotalProfitAndLoss(member.getId(), coins);
        wallet.setUsdtTotalProfitAndLoss(usdtTotalProfitAndLoss);

        MessageResult result = MessageResult.success("success");
        result.setData(wallet);
        return result;
    }

    /**
     * Get current entrust list
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Get current entrust list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("current")
    public Page<ContractOrderEntrust> entrustList(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId, // Contract trading pair
            int pageNo,
            int pageSize) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        IPage<ContractOrderEntrust> contractOrderEntrustOrders = contractOrderEntrustService
                .queryPageEntrustingOrdersBySymbol(member.getId(), contractCoinId, pageNo, pageSize);
        return IPage2Page(contractOrderEntrustOrders);
    }

    /**
     * Get historical entrust list
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Get historical entrust list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("history")
    public Page<ContractOrderEntrust> entrustListHistory(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId, // Contract trading pair
            int pageNo,
            int pageSize) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        IPage<ContractOrderEntrust> contractOrderEntrustOrders = contractOrderEntrustService
                .queryPageEntrustHistoryOrdersBySymbol(member.getId(), contractCoinId, pageNo, pageSize);
        return IPage2Page(contractOrderEntrustOrders);
    }

    /**
     * Whether position mode can be switched (cross margin / isolated margin)
     * Need to check whether there are open isolated/cross orders (if yes, cannot
     * switch)
     *
     * @param authMember
     * @param targetPattern Target mode (1: Cross, 2: Isolated)
     * @return
     */
    @ApiOperation(value = "Check if position mode can be switched (cross margin / isolated margin)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "targetPattern", value = "Position mode"),
    })
    @PermissionOperation
    @RequestMapping("can-switch-pattern")
    public MessageResult canSwitchPattern(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId, ContractOrderPattern targetPattern) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null)
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        ContractCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWallet wallet = memberContractWalletService.findByMemberId(member.getId());
        if (wallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }
        // Check all holding positions
        List<MemberContractPosition> positions = memberContractPositionService.queryAllHoldingPositions(member.getId());
        if (positions != null && positions.size() > 0) {
            return MessageResult.error(500, msService.getMessage("CANCEL_FIRST"));
        }
        MessageResult result = MessageResult.success("success");
        result.setData(null);
        return result;
    }

    /**
     * Switch position mode
     * Since this is a Gold Standard contract, switching position mode will switch
     * all pairs' position modes,
     * so it will check all non-target mode positions to determine whether switching
     * is possible
     *
     * @param authMember
     * @param contractCoinId
     * @param targetPattern
     * @return
     */
    @ApiOperation(value = "Switch position mode")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "targetPattern", value = "Position mode"),
    })
    @PermissionOperation
    @RequestMapping("switch-pattern")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult switchPattern(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId, ContractOrderPattern targetPattern) {

        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null)
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        ContractCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWallet memberContractWallet = memberContractWalletService.findByMemberId(member.getId());
        if (memberContractWallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }

        if (targetPattern != ContractOrderPattern.FIXED && targetPattern != ContractOrderPattern.CROSSED) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        // Check if there are entrusting orders under current contract
        List<MemberContractPosition> positions = memberContractPositionService.queryAllHoldingPositions(member.getId());
        if (positions != null && positions.size() > 0) {
            return MessageResult.error(500, msService.getMessage("CANCEL_FIRST"));
        }

        memberContractWallet.setUsdtPattern(targetPattern);
        boolean save = memberContractWalletService.saveOrUpdate(memberContractWallet);

        if (save) {

            // Notify wallet change
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", memberContractWallet.getId());
            rocketMQTemplate.convertAndSend("member-wallet-change", JSON.toJSONString(jsonObj));

            MessageResult result = MessageResult.success(msService.getMessage("POSITION_MODE1"));
            result.setData(memberContractWallet);
            return result;
        } else {
            return MessageResult.error(500, msService.getMessage("POSITION_MODE2"));
        }
    }

    /**
     * Modify contract leverage multiplier for a specific trading pair
     * Adjusting the leverage multiplier should theoretically release excess margin
     * from existing positions, but this feature has not been implemented yet.
     * For now, adjusting leverage only affects how many contracts can be opened
     * with the same margin.
     *
     * @param authMember
     * @param contractCoinId
     * @param leverage
     * @param direction
     * @return
     */
    @ApiOperation(value = "Modify leverage multiplier for a specific trading pair")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "leverage", value = "Leverage"),
            @ApiImplicitParam(name = "direction", value = "Direction"),
    })
    @PermissionOperation
    @RequestMapping("modify-leverage")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult modifyLeverage(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId,
            BigDecimal leverage,
            ContractOrderDirection direction) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null)
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        ContractCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWallet memberContractWallet = memberContractWalletService.findByMemberId(member.getId());
        if (memberContractWallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }

        // Check if leverage multiplier is within allowed range
        if (contractCoin.getLeverageType() == 1) {
            // Separate multipliers
            String[] leverageArr = contractCoin.getLeverage().split(",");
            boolean isExist = false;
            for (String str : leverageArr) {
                if (BigDecimal.valueOf(Integer.parseInt(str)).compareTo(leverage) == 0) {
                    isExist = true;
                }
            }
            if (!isExist) {
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_NOT_EXIST"));
            }
        } else { // Range multipliers
            String[] leverageArr = contractCoin.getLeverage().split(",");
            if (leverageArr.length != 2)
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_ERROR"));

            BigDecimal low = BigDecimal.valueOf(Integer.parseInt(leverageArr[0]));
            BigDecimal high = BigDecimal.valueOf(Integer.parseInt(leverageArr[1]));
            if (leverage.compareTo(low) < 0 || leverage.compareTo(high) > 0) {
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_NOT_ALLOWED"));
            }
        }

        List<MemberContractPosition> positions = memberContractPositionService.queryAllHoldingPositions(member.getId());
        if (positions != null && positions.size() > 0) {
            return MessageResult.error(500, msService.getMessage("CANCEL_FIRST"));
        }

        // If isolated margin mode and leverage is increased, need to check margin
        // sufficiency
        if (direction == ContractOrderDirection.BUY) { // Adjust buy leverage
            memberContractWalletService.modifyUsdtBuyLeverage(memberContractWallet.getId(), leverage);
        } else { // Adjust sell leverage
            memberContractWalletService.modifyUsdtSellLeverage(memberContractWallet.getId(), leverage);
        }
        // Notify wallet change
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("symbol", contractCoin.getSymbol());
        jsonObj.put("walletId", memberContractWallet.getId());
        rocketMQTemplate.convertAndSend("member-wallet-change", JSON.toJSONString(jsonObj));

        MessageResult result = MessageResult.success("success");
        result.setData(null);
        return result;
    }

    private BigDecimal computeForcePriceFixed(MemberContractPosition position, ContractCoin contractCoin) {
        BigDecimal closeFee = position.getPosition().multiply(position.getShareNumber()).multiply(position.getPrice())
                .multiply(contractCoin.getCloseFee());
        BigDecimal forcePrice;
        // Initial margin ratio = margin / ( contract face value * average open price *
        // open volume )
        BigDecimal value = position.getPosition().multiply(position.getShareNumber()).multiply(position.getPrice());
        BigDecimal rate = position.getPrincipalAmount().divide(value, 8, BigDecimal.ROUND_DOWN);

        if (position.getDirection().equals(ContractOrderDirection.BUY)) {
            // Forced liquidation price (long):
            // = open price * (1 - initial margin ratio + maintenance margin ratio) + close
            // fee/(face value*volume)
            forcePrice = position.getPrice()
                    .multiply(BigDecimal.ONE.subtract(rate).add(contractCoin.getMaintenanceMarginRate()))
                    .add(closeFee.divide(position.getShareNumber().multiply(position.getPosition()), 8,
                            BigDecimal.ROUND_DOWN));
        } else {
            // Forced liquidation price (short):
            // = open price * (1 + initial margin ratio - maintenance margin ratio) + close
            // fee/(face value*volume)
            forcePrice = position.getPrice()
                    .multiply(BigDecimal.ONE.add(rate).subtract(contractCoin.getMaintenanceMarginRate()))
                    .add(closeFee.divide(position.getShareNumber().multiply(position.getPosition()), 8,
                            BigDecimal.ROUND_DOWN));
        }

        if (forcePrice.compareTo(BigDecimal.ZERO) == -1) {
            forcePrice = BigDecimal.ZERO;
        }
        return forcePrice;
    }

    /**
     * Adjust margin
     *
     * @param authMember
     * @param principal
     * @param type       0: Increase 1: Decrease
     * @return
     */
    @ApiOperation(value = "Adjust margin")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "principal", value = "Margin"),
            @ApiImplicitParam(name = "direction", value = "Direction"),
            @ApiImplicitParam(name = "type", value = "0: Increase  1: Decrease"),
    })
    @PermissionOperation
    @PostMapping("ajust-principal")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult ajustPrincipal(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "positionId") Long positionId, // Position ID
            @RequestParam(value = "principal") BigDecimal principal,
            @RequestParam(value = "type") Integer type) {

        MemberContractPosition position = memberContractPositionService.getById(positionId);
        if (position == null) {
            return MessageResult.error(500, msService.getMessage("ORDER_NO_EXIST"));
        }
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null)
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        ContractCoin contractCoin = contractCoinService.getById(position.getContractId());
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWallet memberContractWallet = memberContractWalletService.findByMemberId(member.getId());
        if (memberContractWallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }
        if (memberContractWallet.getUsdtPattern() == ContractOrderPattern.CROSSED) {
            return MessageResult.error(500, msService.getMessage("FULL_WAREHOUSE_MODE"));
        }

        // If decrease margin
        if (type == 1) {
            BigDecimal sy = position.getPrincipalAmount().subtract(position.getFrozenPrincipalAmount())
                    .subtract(principal);
            if (position.getInitPrincipalAmount().compareTo(sy) == -1) {
                return MessageResult.error(500, msService.getMessage("INSUFFICIENT_MARGIN_UNABLE_ADJUST"));
            }
            position.setPrincipalAmount(position.getPrincipalAmount().subtract(principal));
            position.setForcePrice(computeForcePriceFixed(position, contractCoin));
            memberContractPositionService.updateById(position);
            memberContractWalletService.increaseUsdtBalance(memberContractWallet.getId(), principal);
            return MessageResult.success("success");

        } else { // Increase margin
            if (memberContractWallet.getUsdtBalance().compareTo(principal) < 0) {
                return MessageResult.error(500, msService.getMessage("INSUFFICIENT_MARGIN_UNABLE_ADJUST"));
            }
            position.setPrincipalAmount(position.getPrincipalAmount().add(principal));
            position.setForcePrice(computeForcePriceFixed(position, contractCoin));
            memberContractPositionService.updateById(position);
            memberContractWalletService.decreaseUsdtBalance(memberContractWallet.getId(), principal);
            return MessageResult.success("success");
        }
    }

    /**
     * Get current holding positions
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Get current holdings")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("holding")
    public MessageResult holdingPositions(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long contractCoinId, // Contract trading pair
            int pageNo,
            int pageSize) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        IPage<MemberContractPosition> positions = memberContractPositionService
                .queryPageHoldingPositions(member.getId(), contractCoinId, pageNo, pageSize);
        return success(IPage2Page(positions));
    }

    /**
     * Set take-profit and stop-loss prices
     *
     * @param authMember
     * @return
     */
    @ApiOperation(value = "Set take-profit and stop-loss prices")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "positionId", value = "Position ID"),
            @ApiImplicitParam(name = "minPrice", value = "minPrice"),
            @ApiImplicitParam(name = "maxPrice", value = "maxPrice"),
    })
    @PermissionOperation
    @RequestMapping("setZYZS")
    public MessageResult setZYZS(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam(value = "positionId") Long positionId, // Position ID
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice) {
        // Check user validity
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        memberContractPositionService.setZYZS(member.getId(), positionId, minPrice, maxPrice);
        return MessageResult.success("success");
    }

}

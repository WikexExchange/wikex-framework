package com.wikex.wikex.swap.feign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.config.SnowflakeConfig;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.ContractOrderEntrustScreen;
import com.wikex.wikex.screen.PageParam;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Entrusted Order Processing Class
 */
@Api(tags = "Entrusted Order Processing Class")
@Slf4j
@RestController
@RequestMapping("/orderFeign")
public class OrderFeignController extends BaseController {

    @Autowired
    private ContractOrderEntrustService contractOrderEntrustService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private MemberContractWalletService memberContractWalletService;
    @Autowired
    private MemberTradeLimitService memberTradeLimitService;

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;
    @Autowired
    private MemberContractPositionService memberContractPositionService;
    @Autowired
    private SnowflakeConfig snowflakeConfig;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    private String openRedisKey = "U_OPEN_%s_%s";

    @Autowired
    private MemberFeign memberFeign;

    @PostMapping("page-query")
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ContractOrderEntrust> pageQuery(@RequestBody ContractOrderEntrustScreen screen){
        return contractOrderEntrustService.pageQuery(screen);
    }

//    @ApiOperation(value = "Find by perpetual contract entrust ID")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "id", value = "id"),
//    })
    @GetMapping("findOne")
    public ContractOrderEntrust findOne(@RequestParam("id") Long id){
        return contractOrderEntrustService.getById(id);
    }

    @PostMapping("findAll4Agent")
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ContractOrderEntrust> findAll4Agent(@RequestParam("memberId") Long memberId, @RequestParam("pageParam") PageParam pageParam, @RequestBody ContractOrderEntrustScreen screen){
        return contractOrderEntrustService.findAll4Agent(memberId, pageParam, screen);
    }

    @PostMapping("sendReward")
    void sendReward(){
        contractOrderEntrustService.sendReward();
    }

    /**
     * Place Contract Order
     * @param
     * @return
     */
    @ApiOperation(value = "Place Contract Order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract Trading Pair"),
            @ApiImplicitParam(name = "direction", value = "1: Buy (Close Short)  2: Sell (Close Long)"),
            @ApiImplicitParam(name = "type", value = "0: Market  1: Limit  2: Planned Entrust"),
            @ApiImplicitParam(name = "triggerPrice", value = "Trigger Price"),
            @ApiImplicitParam(name = "entrustPrice", value = "Entrust Price (if 0 for planned entrust: execute at market price)"),
            @ApiImplicitParam(name = "leverage", value = "Leverage Multiple"),
            @ApiImplicitParam(name = "principalAmount", value = "Entrust Margin"),
    })
    @RequestMapping("insertOrder")
    @GlobalTransactional(rollbackFor = Exception.class)
    MessageResult insertOrder(@RequestParam(value = "memberId") Long memberId,
                              @RequestParam(value = "contractCoinId") Long contractCoinId,
                              @RequestParam(value = "direction") ContractOrderDirection direction,
                              @RequestParam(value = "type") ContractOrderType type,
                              @RequestParam(value = "triggerPrice", required = false) BigDecimal triggerPrice,
                              @RequestParam(value = "entrustPrice") BigDecimal entrustPrice,
                              @RequestParam(value = "leverage") BigDecimal leverage,
                              @RequestParam(value = "principalAmount") BigDecimal principalAmount,
                              @RequestParam(value = "pattern") ContractOrderPattern targetPattern){

        // Input validity check
        if (contractCoinId == null || direction == null || type == null || leverage == null || principalAmount == null) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        if (direction != ContractOrderDirection.BUY && direction != ContractOrderDirection.SELL) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        if (type != ContractOrderType.MARKET_PRICE && type != ContractOrderType.LIMIT_PRICE && type != ContractOrderType.SPOT_LIMIT) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }

        // Check if trading pair exists
        ContractCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }

        // Acquire redis lock
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(openRedisKey,memberId,contractCoinId);
        String redisVal = ops.get(key);
        if(redisVal!=null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key,"11",3, TimeUnit.MINUTES);// 3 minutes

        Member member = memberFeign.findMemberById(memberId);
        if (member == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        // Check if user wallet exists
        MemberContractWallet memberContractWallet = memberContractWalletService.findByMemberId(member.getId());
        if (memberContractWallet == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("WALLET_NOT_IN"));
        }else {
            // Modify wallet position mode
            if(!memberContractWallet.getUsdtPattern().equals(targetPattern)) {
                if (targetPattern != ContractOrderPattern.FIXED && targetPattern != ContractOrderPattern.CROSSED) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
                }
                // Query whether the current contract has any orders being entrusted
                List<MemberContractPosition> positions = memberContractPositionService.queryAllHoldingPositions(member.getId());
                if (positions != null && positions.size() > 0) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("CANCEL_FIRST"));
                }
                memberContractWallet.setUsdtPattern(targetPattern);

            }
            if(direction.equals(ContractOrderDirection.BUY)){
                memberContractWallet.setUsdtBuyLeverage(leverage);
            }else {
                memberContractWallet.setUsdtSellLeverage(leverage);
            }
            // Save contract leverage
            memberContractWalletService.updateById(memberContractWallet);
        }

        // Set open fee
        MemberTradeLimit limit = memberTradeLimitService.findLimitByMemberIdAndContractId(member.getId(),contractCoinId);
        if(limit!=null) {
            // Set open fee
            BigDecimal memberFee = limit.getOpenFee();
            if (memberFee != null) {
                contractCoin.setOpenFee(memberFee);
            }
            // Set slippage
            if (limit.getSpreadType() != null) {
                contractCoin.setSpreadType(limit.getSpreadType());
            }
            if (limit.getSpread() != null && limit.getSpread().compareTo(BigDecimal.ZERO) > 0) {
                contractCoin.setSpread(limit.getSpread());
            }
        }

        // Get leverage multiple
//        leverage = direction == ContractOrderDirection.BUY ? memberContractWallet.getUsdtBuyLeverage() : memberContractWallet.getUsdtSellLeverage();
        // Limit orders and planned entrust orders require entrust price
        if (type == ContractOrderType.LIMIT_PRICE || type == ContractOrderType.SPOT_LIMIT) {
            if (entrustPrice == null) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("INPUT_COMMISSION_PRICE"));
            }
        }

        // Whether the user is prohibited from trading
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE)) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CANNOT_TRADE"));
        }

        // Check whether the trading pair is available
        if (contractCoin.getEnable() != 1 || contractCoin.getExchangeable() != 1) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("COIN_FORBIDDEN"));
        }
        // Whether long opening (buy up) is allowed
        if (contractCoin.getEnableOpenBuy() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.BUY) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_LONG1"));
        }
        // Whether short opening (buy down) is allowed
        if (contractCoin.getEnableOpenSell() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.SELL) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_SHORT1"));
        }
        // Whether market long opening is allowed
        if (contractCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.BUY) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_LONG2"));
        }
        // Whether market short opening is allowed
        if (contractCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.SELL) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_SHORT2"));
        }

        // Whether leverage multiple is within the allowed range
        if (contractCoin.getLeverageType() == 1) { // Discrete multiples
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
        } else { // Range multiples
            String[] leverageArr = contractCoin.getLeverage().split(",");
            if (leverageArr.length != 2) return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_ERROR"));

            BigDecimal low = BigDecimal.valueOf(Integer.parseInt(leverageArr[0]));
            BigDecimal high = BigDecimal.valueOf(Integer.parseInt(leverageArr[1]));
            if (leverage.compareTo(low) < 0 || leverage.compareTo(high) > 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_NOT_ALLOWED"));
            }
        }
        // Check whether the order quantity is within the range

        // Check whether the contract engine exists
        if (!contractCoinMatchFactory.containsContractCoinMatch(contractCoin.getSymbol())) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_ENGINE_ERROR"));
        }

        // Calculate opening price (for slippage > market price)
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(contractCoin.getSymbol()).getNowPrice();
        // Whether the entrust price is too high or too low (limit orders must be placed within a 2% price range)
        if(type == ContractOrderType.LIMIT_PRICE) {
            if(entrustPrice.compareTo(currentPrice.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.2)))) > 0
                    || entrustPrice.compareTo(currentPrice.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(0.2)))) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("ORDER_PRICE_OVERRUN"));
            }
        }

        openPrice = currentPrice;
        // Calculate market execution price (approximate)
        if(type == ContractOrderType.MARKET_PRICE){
            if (direction == ContractOrderDirection.BUY) { // Buy, slippage calculation, go long, execute at a higher price
                if (contractCoin.getSpreadType() == 1) { // Slippage type: percentage
                    openPrice = currentPrice.add(currentPrice.multiply(contractCoin.getSpread())); // Execute at current price (or slippage price)
                } else { // Slippage type: fixed amount
                    openPrice = currentPrice.add(contractCoin.getSpread());
                }
            } else { // Sell, slippage calculation, go short, execute at a lower price
                if (contractCoin.getSpreadType() == 1) { // Slippage type: percentage
                    openPrice = currentPrice.subtract(currentPrice.multiply(contractCoin.getSpread())); // Execute at current price (or slippage price)
                } else { // Slippage type: fixed amount
                    openPrice = currentPrice.subtract(contractCoin.getSpread());
                }
            }
        }

        if (openPrice.compareTo(BigDecimal.ZERO) <= 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_ENGINE_ERROR"));
        }

        // Opening position, check whether margin is sufficient
        /**
         * The following code involves the calculation of contract margin.
         * For the opening operation, it is necessary to calculate the margin
         * of all holding orders and entrusted orders, not limited to this currency.
         *
         */
        // 0. Calculate the margin required for the current opening order
        // Contract lots * Contract face value / Leverage (applies to USDt-margined mode)
        BigDecimal volume = principalAmount.multiply(leverage).divide(contractCoin.getShareNumber().multiply(currentPrice),8,BigDecimal.ROUND_DOWN);

        // 1. Calculate opening fee (Contract lots * Contract face value * Open fee rate)
        BigDecimal openFee = principalAmount.multiply(leverage).multiply(contractCoin.getOpenFee());

        // For isolated mode, only compare available balance
        if (memberContractWallet.getUsdtPattern() == ContractOrderPattern.FIXED) {
            if (principalAmount.add(openFee).compareTo(memberContractWallet.getUsdtBalance()) > 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
            }
        }
        // For cross mode, calculate total equity of short and long positions
        if (memberContractWallet.getUsdtPattern() == ContractOrderPattern.CROSSED) {
            // Calculate USDT-based equity (long + short)
            List<ContractCoin> coins =  contractCoinService.list();
            BigDecimal usdtTotalProfitAndLoss = memberContractWalletService.usdtTotalProfitAndLoss(member.getId(), coins);
            // If the above calculation yields a negative value, since it is cross mode,
            // subtract this value from the balance, then check whether the balance is sufficient
            if (usdtTotalProfitAndLoss.compareTo(BigDecimal.ZERO) < 0) {
                if (principalAmount.add(openFee).compareTo(memberContractWallet.getUsdtBalance().add(usdtTotalProfitAndLoss)) > 0) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
                }
            } else { // If the holding equity is positive, compare directly with available balance
                if (principalAmount.add(openFee).compareTo(memberContractWallet.getUsdtBalance().add(usdtTotalProfitAndLoss)) > 0) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
                }
            }
        }

        // In planned entrust, the trigger price must be greater than 0
        if(type == ContractOrderType.SPOT_LIMIT) {
            if(triggerPrice.compareTo(BigDecimal.ZERO) <= 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("TRIGGER_PRICE1"));
            }
            if(entrustPrice.compareTo(BigDecimal.ZERO) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("TRIGGER_PRICE2"));
            }
        }

        if(type == ContractOrderType.LIMIT_PRICE || type == ContractOrderType.MARKET_PRICE) {
            MessageResult result = memberContractWalletService.freezeUsdtBalance(memberContractWallet, principalAmount.add(openFee));;
            if(result.getCode()!=0){
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
            }
        }else{
            // Planned entrust: only one order is allowed in the same direction (e.g., only one take-profit or stop-loss)
        }

        // Create new contract entrust order
        ContractOrderEntrust orderEntrust = new ContractOrderEntrust();
        orderEntrust.setContractId(contractCoin.getId()); // Contract ID
        orderEntrust.setMemberId(member.getId()); // User ID
        orderEntrust.setSymbol(contractCoin.getSymbol()); // Trading pair symbol
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); // Base/Settlement currency
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); // Coin symbol
        orderEntrust.setDirection(direction); // Opening direction: long/short
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));
        orderEntrust.setVolume(volume); // Opening lots
        orderEntrust.setTradedVolume(BigDecimal.ZERO); // Traded quantity
        orderEntrust.setTradedPrice(BigDecimal.ZERO); // Execution price
        orderEntrust.setPrincipalUnit("USDT"); // Margin unit
        orderEntrust.setPrincipalAmount(principalAmount); // Margin amount
        orderEntrust.setCreateTime(DateUtil.getTimeMillis()); // Opening time
        orderEntrust.setLeverage(leverage);
        orderEntrust.setType(type);
        orderEntrust.setTriggerPrice(triggerPrice); // Trigger price
        orderEntrust.setEntrustPrice(entrustPrice); // Entrust price
        orderEntrust.setEntrustType(ContractOrderEntrustType.OPEN); // Open position
        orderEntrust.setTriggeringTime(0L); // Trigger time, temporarily unused
        orderEntrust.setShareNumber(contractCoin.getShareNumber());
        orderEntrust.setProfitAndLoss(BigDecimal.ZERO); // PnL (only for closing)
        orderEntrust.setPatterns(memberContractWallet.getUsdtPattern()); // Position mode
        orderEntrust.setOpenFee(openFee); // Open fee
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_ING); // Entrust status: entrusting
        orderEntrust.setCurrentPrice(currentPrice);
        orderEntrust.setIsBlast(0); // Not a liquidation order
        if(type == ContractOrderType.SPOT_LIMIT) { // Whether it is a planned entrust
            orderEntrust.setIsFromSpot(1);
        }else{
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
            // Return result
            MessageResult result = MessageResult.error(msService.getMessage("SWAP_FAILED"));
            result.setData(null);
            redisTemplate.delete(key);
            return result;
        }
    }

}

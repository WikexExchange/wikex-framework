package com.wikex.wikex.coinswap.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.ContractOrderEntrustCoinService;
import com.wikex.wikex.coinswap.service.MemberContractWalletCoinService;
import com.wikex.wikex.config.SnowflakeConfig;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.ContractOrderEntrustScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
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
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private ContractCoinCoinService contractCoinService;
    @Autowired
    private ContractOrderEntrustCoinService contractOrderEntrustService;
    @Autowired
    private MemberContractWalletCoinService memberContractWalletService;
    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private SnowflakeConfig snowflakeConfig;
    @Autowired
    private RedisTemplate redisTemplate;
    private String openRedisKey = "COINSWAP_OPEN_%s_%s";
    private String closeRedisKey = "COINSWAP_CLOSE_%s_%s";
    private String cancelRedisKey = "COINSWAP_CANCEL_%s_%s";
    private String cancelAllRedisKey = "COINSWAP_CANCEL_ALL_%s_%s";
    private String closeAllRedisKey = "COINSWAP_CLOSE_ALL_%s_%s";
    
    @ApiOperation(value = "Place contract order (open position) – coin-margined. Two operation types: buy to open long, sell to open long")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "direction", value = "1: Buy (close short)  2: Sell (close long)"),
            @ApiImplicitParam(name = "type", value = "0: Market 1: Limit 2: Plan (trigger) order"),
            @ApiImplicitParam(name = "triggerPrice", value = "Trigger price"),
            @ApiImplicitParam(name = "entrustPrice", value = "Entrust price (for plan order, if 0: execute at market)"),
            @ApiImplicitParam(name = "leverage", value = "Entrust price"),
            @ApiImplicitParam(name = "volume", value = "Order quantity (contracts)"),
    })
    @PermissionOperation
    @RequestMapping("open")
    @GlobalTransactional(rollbackFor = Exception.class)
    public MessageResult openOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                   @RequestParam(value = "contractCoinId") Long contractCoinId,
                                   @RequestParam(value = "direction") ContractOrderDirection direction,
                                   @RequestParam(value = "type") ContractOrderType type,
                                   @RequestParam(value = "triggerPrice", required = false) BigDecimal triggerPrice,
                                   @RequestParam(value = "entrustPrice") BigDecimal entrustPrice,
                                   @RequestParam(value = "leverage") BigDecimal leverage,
                                   @RequestParam(value = "volume") BigDecimal volume
    ) {
        
        if (contractCoinId == null || direction == null || type == null || leverage == null || volume == null) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        if (direction != ContractOrderDirection.BUY && direction != ContractOrderDirection.SELL) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        if (type != ContractOrderType.MARKET_PRICE && type != ContractOrderType.LIMIT_PRICE && type != ContractOrderType.SPOT_LIMIT) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }

        AuthMember user = AuthMember.toAuthMember(authMember);
        
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(openRedisKey,user.getId(),contractCoinId);
        String redisVal = ops.get(key);
        if(redisVal!=null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key,"11",3, TimeUnit.MINUTES);
        Member member = memberFeign.findMemberById(user.getId());
        if(member == null) {
            redisTemplate.delete(key);
            return MessageResult.error(msService.getMessage("ACCOUNT_NOT_EXIST"));
        }

        
        ContractCoinCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }
        
        MemberContractWalletCoin memberContractWallet = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(), contractCoin);
        if (memberContractWallet == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("WALLET_NOT_IN"));
        }
        
        BigDecimal walletPosition = direction == ContractOrderDirection.BUY ? memberContractWallet.getCoinBuyPosition().add(memberContractWallet.getCoinFrozenBuyPosition()) : memberContractWallet.getCoinSellPosition().add(memberContractWallet.getCoinFrozenSellPosition());
        
        if(walletPosition.compareTo(BigDecimal.ZERO) == 0) {
            if(direction == ContractOrderDirection.BUY) {
                memberContractWalletService.modifyCoinBuyLeverage(memberContractWallet.getId(), leverage);
            }else{
                memberContractWalletService.modifyCoinSellLeverage(memberContractWallet.getId(), leverage);
            }
        }
        ContractOrderPattern pattern = memberContractWallet.getCoinPattern();
        

        
        if (type == ContractOrderType.LIMIT_PRICE || type == ContractOrderType.SPOT_LIMIT) {
            if (entrustPrice == null) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("INPUT_COMMISSION_PRICE"));
            }
        }
        
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE.getCode())) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CANNOT_TRADE"));
        }

        
        if (contractCoin.getEnable() != 1 || contractCoin.getExchangeable() != 1) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("COIN_FORBIDDEN"));
        }
        
        if (contractCoin.getEnableOpenBuy() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.BUY) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_LONG1"));
        }
        
        if (contractCoin.getEnableOpenSell() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.SELL) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_SHORT1"));
        }
        
        if (contractCoin.getEnableMarketBuy() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.BUY) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_LONG2"));
        }
        
        if (contractCoin.getEnableMarketSell() == BooleanEnum.IS_FALSE && direction == ContractOrderDirection.SELL) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SUSPEND_SHORT2"));
        }

        
        if (contractCoin.getLeverageType() == 1) { 
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
        } else { 
            String[] leverageArr = contractCoin.getLeverage().split(",");
            if (leverageArr.length != 2) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_ERROR"));
            }

            BigDecimal low = BigDecimal.valueOf(Integer.parseInt(leverageArr[0]));
            BigDecimal high = BigDecimal.valueOf(Integer.parseInt(leverageArr[1]));
            if (leverage.compareTo(low) < 0 || leverage.compareTo(high) > 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_NOT_ALLOWED"));
            }
        }
        
        if (volume.compareTo(contractCoin.getMinShare()) < 0){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NO_LESS_THAN") + contractCoin.getMinShare() + msService.getMessage("LOT"));
        }
        if (volume.compareTo(contractCoin.getMaxShare()) > 0){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NO_HIGHER_THAN") + contractCoin.getMaxShare() + msService.getMessage("LOT"));
        }
        if(volume.compareTo(BigDecimal.ONE) < 0){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("INCORRECT_OPENING_NUMBER"));
        }
        if(BigDecimal.valueOf(volume.intValue()).compareTo(volume) != 0){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("INCORRECT_OPENING_NUMBER")); 
        }
        
        if (!contractCoinMatchFactory.containsContractCoinMatch(contractCoin.getSymbol())) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_ENGINE_ERROR"));
        }

        
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(contractCoin.getSymbol()).getNowPrice();
        
        if(type == ContractOrderType.LIMIT_PRICE) {
            if(entrustPrice.compareTo(currentPrice.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.2)))) > 0
                    || entrustPrice.compareTo(currentPrice.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(0.2)))) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("ORDER_PRICE_OVERRUN"));
            }else {
                openPrice = entrustPrice;
            }
        }else {
            
            openPrice = currentPrice;
        }

        if (direction == ContractOrderDirection.BUY) { 
            if (contractCoin.getSpreadType() == 1) { 
                openPrice = openPrice.add(openPrice.multiply(contractCoin.getSpread())); 
            } else { 
                openPrice = openPrice.add(contractCoin.getSpread());
            }
        } else { 
            if (contractCoin.getSpreadType() == 1) { 
                openPrice = openPrice.subtract(openPrice.multiply(contractCoin.getSpread())); 
            } else { 
                openPrice = currentPrice.subtract(contractCoin.getSpread());
            }
        }

        if (openPrice.compareTo(BigDecimal.ZERO) <= 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_ENGINE_ERROR"));
        }

        
        
        
        
        BigDecimal principalAmount = volume.multiply(contractCoin.getShareNumber()).divide(leverage.multiply(openPrice), 8, BigDecimal.ROUND_DOWN);

        
        BigDecimal openFee = volume.multiply(contractCoin.getShareNumber()).divide(openPrice, 8, BigDecimal.ROUND_DOWN).multiply(contractCoin.getOpenFee());

        
        if (memberContractWallet.getCoinPattern() == ContractOrderPattern.FIXED) {
            if (principalAmount.add(openFee).compareTo(memberContractWallet.getCoinBalance()) > 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
            }
        }
        
        if (memberContractWallet.getCoinPattern() == ContractOrderPattern.CROSSED) {
            
            BigDecimal coinTotalProfitAndLoss = BigDecimal.ZERO;
            
            
            
            if (memberContractWallet.getCoinBuyPrice().compareTo(BigDecimal.ZERO) > 0 && memberContractWallet.getCoinBuyPosition().compareTo(BigDecimal.ZERO) > 0) {





                BigDecimal buyPL = memberContractWallet.getCoinShareNumber().multiply(
                        memberContractWallet.getCoinBuyPosition().add(memberContractWallet.getCoinFrozenBuyPosition())
                ).divide(memberContractWallet.getCoinBuyPrice(),8,BigDecimal.ROUND_DOWN).multiply(openPrice.subtract(memberContractWallet.getCoinBuyPrice()));

                coinTotalProfitAndLoss = coinTotalProfitAndLoss.add(buyPL);
            }

            
            
            if (memberContractWallet.getCoinSellPrice().compareTo(BigDecimal.ZERO) > 0 && memberContractWallet.getCoinSellPosition().compareTo(BigDecimal.ZERO) > 0) {
                








                BigDecimal sellPL = memberContractWallet.getCoinShareNumber().multiply(
                        memberContractWallet.getCoinSellPosition().add(memberContractWallet.getCoinFrozenSellPosition())
                ).divide(memberContractWallet.getCoinSellPrice(),8,BigDecimal.ROUND_DOWN).multiply(memberContractWallet.getCoinSellPrice().subtract(openPrice));


                coinTotalProfitAndLoss = coinTotalProfitAndLoss.add(sellPL);
            }

            
            coinTotalProfitAndLoss = coinTotalProfitAndLoss.divide(openPrice,4,BigDecimal.ROUND_DOWN).add(memberContractWallet.getCoinBuyPrincipalAmount()).add(memberContractWallet.getCoinSellPrincipalAmount());
            
            if (coinTotalProfitAndLoss.compareTo(BigDecimal.ZERO) < 0) {
                if (principalAmount.add(openFee).compareTo(memberContractWallet.getCoinBalance().add(coinTotalProfitAndLoss)) > 0) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
                }
            } else { 
                if (principalAmount.add(openFee).compareTo(memberContractWallet.getCoinBalance()) > 0) {
                    redisTemplate.delete(key);
                    return MessageResult.error(500, msService.getMessage("SWAP_BALANCE_NOT_ENOUGH"));
                }
            }
        }

        
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
        
        ContractOrderEntrustCoin orderEntrust = new ContractOrderEntrustCoin();
        orderEntrust.setContractId(contractCoin.getId()); 
        orderEntrust.setMemberId(member.getId()); 
        orderEntrust.setSymbol(contractCoin.getSymbol()); 
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); 
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); 
        orderEntrust.setDirection(direction); 
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));
        orderEntrust.setVolume(volume); 
        orderEntrust.setTradedVolume(BigDecimal.ZERO); 
        orderEntrust.setTradedPrice(BigDecimal.ZERO); 
        orderEntrust.setPrincipalUnit(contractCoin.getCoinSymbol()); 
        orderEntrust.setPrincipalAmount(principalAmount); 
        orderEntrust.setCreateTime(DateUtil.getTimeMillis()); 
        orderEntrust.setType(type);
        orderEntrust.setTriggerPrice(triggerPrice); 
        orderEntrust.setEntrustPrice(entrustPrice); 
        orderEntrust.setEntrustType(ContractOrderEntrustType.OPEN); 
        orderEntrust.setTriggeringTime(0L); 
        orderEntrust.setShareNumber(contractCoin.getShareNumber());
        orderEntrust.setProfitAndLoss(BigDecimal.ZERO); 
        orderEntrust.setPatterns(memberContractWallet.getCoinPattern()); 
        orderEntrust.setOpenFee(openFee); 
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_ING); 
        orderEntrust.setCurrentPrice(currentPrice);
        orderEntrust.setIsBlast(0); 
        if(type == ContractOrderType.SPOT_LIMIT) { 
            orderEntrust.setIsFromSpot(1);
        }else{
            orderEntrust.setIsFromSpot(0);
        }

        
        boolean retObj = contractOrderEntrustService.save(orderEntrust);

        
        if(type == ContractOrderType.LIMIT_PRICE || type == ContractOrderType.MARKET_PRICE) {
            memberContractWalletService.freezeCoinBalance(memberContractWallet, principalAmount.add(openFee));
        }else{
            
        }

        if (retObj) {
            
            rocketMQTemplate.convertAndSend("swap-coin-order-open", JSON.toJSONString(orderEntrust));
            
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", memberContractWallet.getId());
            rocketMQTemplate.convertAndSend("member-coin-wallet-change", JSON.toJSONString(jsonObj));

            
            
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

    
    @ApiOperation(value = "Close contract position (coin-margined). Four operation types: buy to close short, sell to close long")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "direction", value = "1: Buy (close short)  2: Sell (close long)"),
            @ApiImplicitParam(name = "type", value = "0: Market 1: Limit 2: Plan (trigger) order"),
            @ApiImplicitParam(name = "triggerPrice", value = "Trigger price"),
            @ApiImplicitParam(name = "entrustPrice", value = "Entrust price (for plan order, if 0: execute at market)"),
            @ApiImplicitParam(name = "volume", value = "Order quantity (contracts)"),
    })
    @PermissionOperation
    @RequestMapping("close")
    @GlobalTransactional(rollbackFor = Exception.class)
    public MessageResult closeOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                    @RequestParam(value = "contractCoinId") Long contractCoinId,
                                    @RequestParam(value = "direction") ContractOrderDirection direction,
                                    @RequestParam(value = "type") ContractOrderType type,
                                    @RequestParam(value = "triggerPrice", required = false) BigDecimal triggerPrice,
                                    @RequestParam(value = "entrustPrice") BigDecimal entrustPrice,
                                    @RequestParam(value = "volume") BigDecimal volume
    ) {
        
        if (contractCoinId == null || direction == null || type == null || volume == null) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        
        ContractCoinCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        
        if (type == ContractOrderType.LIMIT_PRICE || type == ContractOrderType.SPOT_LIMIT) {
            if (entrustPrice == null) {
                return MessageResult.error(500, msService.getMessage("INPUT_COMMISSION_PRICE"));
            }
        }
        AuthMember user = AuthMember.toAuthMember(authMember);
        
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(closeRedisKey,user.getId(),contractCoinId);
        String redisVal = ops.get(key);
        if(redisVal!=null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key,"11",3, TimeUnit.MINUTES);
        Member member = memberFeign.findMemberById(user.getId());
        
        if (member == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        
        if (member.getTransactionStatus().equals(BooleanEnum.IS_FALSE.getCode())) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CANNOT_TRADE"));
        }

        
        MemberContractWalletCoin memberContractWallet = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(), contractCoin);
        if (memberContractWallet == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NONSUPPORT_COIN"));
        }

        if(volume.compareTo(BigDecimal.ONE) < 0){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("INCORRECT_NUMBER_CLOSING_POSITIONS"));
        }
        if(BigDecimal.valueOf(volume.intValue()).compareTo(volume) != 0){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("INCORRECT_NUMBER_CLOSING_POSITIONS")); 
        }
        if (direction == ContractOrderDirection.BUY) {
            
            if (memberContractWallet.getCoinSellPosition().compareTo(volume) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("WRONG_AMOUNT_COMMISSION"));
            }
        } else {
            
            if (memberContractWallet.getCoinBuyPosition().compareTo(volume) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("WRONG_AMOUNT_COMMISSION"));
            }
        }

        BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(contractCoin.getSymbol()).getNowPrice();

        
        if(type == ContractOrderType.LIMIT_PRICE) {
            if(entrustPrice.compareTo(currentPrice.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(0.02)))) > 0
                    || entrustPrice.compareTo(currentPrice.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(0.02)))) < 0) {
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("ORDER_PRICE_OVERRUN"));
            }
        }

        
        BigDecimal closeFee = volume.multiply(contractCoin.getShareNumber()).multiply(contractCoin.getCloseFee());

        
        ContractOrderEntrustCoin orderEntrust = new ContractOrderEntrustCoin();
        orderEntrust.setContractId(contractCoin.getId()); 
        orderEntrust.setMemberId(member.getId()); 
        orderEntrust.setSymbol(contractCoin.getSymbol()); 
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); 
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); 
        orderEntrust.setDirection(direction); 
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));
        orderEntrust.setVolume(volume); 
        orderEntrust.setTradedVolume(BigDecimal.ZERO); 
        orderEntrust.setTradedPrice(BigDecimal.ZERO); 
        orderEntrust.setPrincipalUnit(contractCoin.getCoinSymbol()); 
        orderEntrust.setPrincipalAmount(BigDecimal.ZERO); 
        orderEntrust.setCreateTime(DateUtil.getTimeMillis()); 
        orderEntrust.setType(type);
        orderEntrust.setTriggerPrice(triggerPrice); 
        orderEntrust.setEntrustPrice(entrustPrice); 
        orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE); 
        orderEntrust.setTriggeringTime(0L); 
        orderEntrust.setShareNumber(contractCoin.getShareNumber());
        orderEntrust.setProfitAndLoss(BigDecimal.ZERO); 
        orderEntrust.setPatterns(memberContractWallet.getCoinPattern()); 
        orderEntrust.setCloseFee(closeFee);
        orderEntrust.setCurrentPrice(currentPrice);
        orderEntrust.setIsBlast(0); 
        if(type == ContractOrderType.SPOT_LIMIT) { 
            orderEntrust.setIsFromSpot(1);
        }else{
            orderEntrust.setIsFromSpot(0);
        }
        
        if(type != ContractOrderType.SPOT_LIMIT) {
            if (direction == ContractOrderDirection.BUY) { 
                BigDecimal mPrinc = volume.divide(memberContractWallet.getCoinSellPosition().add(memberContractWallet.getCoinFrozenSellPosition()), 8, RoundingMode.HALF_UP).multiply(memberContractWallet.getCoinSellPrincipalAmount());
                orderEntrust.setPrincipalAmount(mPrinc);
            } else {
                BigDecimal mPrinc = volume.divide(memberContractWallet.getCoinBuyPosition().add(memberContractWallet.getCoinFrozenBuyPosition()), 8, RoundingMode.HALF_UP).multiply(memberContractWallet.getCoinBuyPrincipalAmount());
                orderEntrust.setPrincipalAmount(mPrinc);
            }
        }

        
        BigDecimal dealPrice = currentPrice;
        if (direction == ContractOrderDirection.BUY) { 
            if (contractCoin.getSpreadType() == 1) { 
                dealPrice = currentPrice.add(currentPrice.multiply(contractCoin.getSpread())); 
            } else { 
                dealPrice = currentPrice.add(contractCoin.getSpread());
            }
        } else { 
            if (contractCoin.getSpreadType() == 1) { 
                dealPrice = currentPrice.subtract(currentPrice.multiply(contractCoin.getSpread())); 
            } else { 
                dealPrice = currentPrice.subtract(contractCoin.getSpread());
            }
        }
        
        if (dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_ENGINE_ERROR"));
        }

        
        if(type == ContractOrderType.LIMIT_PRICE || type == ContractOrderType.MARKET_PRICE) {
            
            if (direction == ContractOrderDirection.BUY) {
                
                memberContractWalletService.freezeCoinSellPosition(memberContractWallet.getId(), volume);
            } else {
                
                memberContractWalletService.freezeCoinBuyPosition(memberContractWallet.getId(), volume);
            }
        }

        
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_ING); 
        boolean retObj = contractOrderEntrustService.save(orderEntrust);

        if (retObj) {
            
            rocketMQTemplate.convertAndSend("swap-coin-order-close", JSON.toJSONString(orderEntrust));

            
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", memberContractWallet.getId());
            rocketMQTemplate.convertAndSend("member-coin-wallet-change", JSON.toJSONString(jsonObj));
            
            
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

    
    @ApiOperation(value = "One-click close (market close all)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "type", value = "0: Market close long  1: Market close short  2: Market close long + short"),
    })
    @PermissionOperation
    @RequestMapping("close-all")
    @GlobalTransactional(rollbackFor = Exception.class)
    public MessageResult closeAll(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, Long contractCoinId, Integer type) {
        org.apache.shiro.util.Assert.notNull(contractCoinId, msService.getMessage("CONTRACT_ID"));
        org.apache.shiro.util.Assert.notNull(type, msService.getMessage("CLOSING_TYPE"));
        AuthMember user = AuthMember.toAuthMember(authMember);
        
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(closeAllRedisKey,user.getId(),contractCoinId);
        String redisVal = ops.get(key);
        if(redisVal!=null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key,"11",3, TimeUnit.MINUTES);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        ContractCoinCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }

        MemberContractWalletCoin wallet = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(), contractCoin);
        if (wallet == null) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }

        BigDecimal volumeBuy = BigDecimal.ZERO;
        BigDecimal volumeSell = BigDecimal.ZERO;
        if(type == 0) { 
            
            if(wallet.getCoinFrozenBuyPosition().compareTo(BigDecimal.ZERO) > 0) {
                redisTemplate.delete(key);
                return MessageResult.error(msService.getMessage("CANCEL_CLOSING_ORDERS1"));
            }
            if(wallet.getCoinBuyPosition().compareTo(BigDecimal.ZERO) <= 0) {
                redisTemplate.delete(key);
                return MessageResult.error(msService.getMessage("NO_LONG_ORDER"));
            }
            volumeBuy = wallet.getCoinBuyPosition();
        }else if(type == 1) {
            if(wallet.getCoinFrozenSellPosition().compareTo(BigDecimal.ZERO) > 0) {
                redisTemplate.delete(key);
                return MessageResult.error(msService.getMessage("CANCEL_CLOSING_ORDERS2"));
            }
            if(wallet.getCoinSellPosition().compareTo(BigDecimal.ZERO) <= 0) {
                redisTemplate.delete(key);
                return MessageResult.error(msService.getMessage("NO_SHORT_ORDER"));
            }
            volumeSell = wallet.getCoinSellPosition();
        }else if(type == 2){
            if(wallet.getCoinFrozenSellPosition().compareTo(BigDecimal.ZERO) > 0 || wallet.getCoinFrozenBuyPosition().compareTo(BigDecimal.ZERO) > 0) {
                redisTemplate.delete(key);
                return MessageResult.error(msService.getMessage("CANCEL_CLOSING_ORDERS3"));
            }
            if(wallet.getCoinBuyPosition().compareTo(BigDecimal.ZERO) <= 0 && wallet.getCoinSellPosition().compareTo(BigDecimal.ZERO) <= 0) {
                redisTemplate.delete(key);
                return MessageResult.error(msService.getMessage("NO_LONG_SHORT_ORDERS"));
            }
            volumeBuy = wallet.getCoinBuyPosition();
            volumeSell = wallet.getCoinSellPosition();
        }

        BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(contractCoin.getSymbol()).getNowPrice();

        
        if(type == 0 || type == 2) {
            BigDecimal volume = volumeBuy;
            
            BigDecimal closeFee = volume.multiply(contractCoin.getShareNumber()).multiply(contractCoin.getCloseFee());

            
            ContractOrderEntrustCoin orderEntrust = new ContractOrderEntrustCoin();
            orderEntrust.setContractId(contractCoin.getId()); 
            orderEntrust.setMemberId(member.getId()); 
            orderEntrust.setSymbol(contractCoin.getSymbol()); 
            orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); 
            orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); 
            orderEntrust.setDirection(ContractOrderDirection.SELL); 
            orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));
            orderEntrust.setVolume(volume); 
            orderEntrust.setTradedVolume(BigDecimal.ZERO); 
            orderEntrust.setTradedPrice(BigDecimal.ZERO); 
            orderEntrust.setPrincipalUnit("USDT"); 
            orderEntrust.setPrincipalAmount(BigDecimal.ZERO); 
            orderEntrust.setCreateTime(DateUtil.getTimeMillis()); 
            orderEntrust.setType(ContractOrderType.MARKET_PRICE);
            orderEntrust.setTriggerPrice(BigDecimal.ZERO); 
            orderEntrust.setEntrustPrice(BigDecimal.ZERO); 
            orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE); 
            orderEntrust.setTriggeringTime(0L); 
            orderEntrust.setShareNumber(contractCoin.getShareNumber());
            orderEntrust.setProfitAndLoss(BigDecimal.ZERO); 
            orderEntrust.setPatterns(wallet.getCoinPattern()); 
            orderEntrust.setCloseFee(closeFee);
            orderEntrust.setCurrentPrice(currentPrice);
            orderEntrust.setIsBlast(0); 
            orderEntrust.setIsFromSpot(0);
            orderEntrust.setPrincipalAmount(wallet.getCoinBuyPrincipalAmount()); 

            memberContractWalletService.freezeCoinBuyPosition(wallet.getId(), volume);

            
            orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_ING); 
            boolean retObj = contractOrderEntrustService.save(orderEntrust);

            if (retObj) {
                
                rocketMQTemplate.convertAndSend("swap-coin-order-close", JSON.toJSONString(orderEntrust));

                
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("symbol", contractCoin.getSymbol());
                jsonObj.put("walletId", wallet.getId());
                rocketMQTemplate.convertAndSend("member-coin-wallet-change", JSON.toJSONString(jsonObj));
                
                
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


        
        if(type == 1 || type == 2) {
            BigDecimal volume = volumeSell;
            
            BigDecimal closeFee = volume.multiply(contractCoin.getShareNumber()).multiply(contractCoin.getCloseFee());

            
            ContractOrderEntrustCoin orderEntrust = new ContractOrderEntrustCoin();
            orderEntrust.setContractId(contractCoin.getId()); 
            orderEntrust.setMemberId(member.getId()); 
            orderEntrust.setSymbol(contractCoin.getSymbol()); 
            orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); 
            orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); 
            orderEntrust.setDirection(ContractOrderDirection.BUY); 
            orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));
            orderEntrust.setVolume(volume); 
            orderEntrust.setTradedVolume(BigDecimal.ZERO); 
            orderEntrust.setTradedPrice(BigDecimal.ZERO); 
            orderEntrust.setPrincipalUnit("USDT"); 
            orderEntrust.setPrincipalAmount(BigDecimal.ZERO); 
            orderEntrust.setCreateTime(DateUtil.getTimeMillis()); 
            orderEntrust.setType(ContractOrderType.MARKET_PRICE);
            orderEntrust.setTriggerPrice(BigDecimal.ZERO); 
            orderEntrust.setEntrustPrice(BigDecimal.ZERO); 
            orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE); 
            orderEntrust.setTriggeringTime(0L); 
            orderEntrust.setShareNumber(contractCoin.getShareNumber());
            orderEntrust.setProfitAndLoss(BigDecimal.ZERO); 
            orderEntrust.setPatterns(wallet.getCoinPattern()); 
            orderEntrust.setCloseFee(closeFee);
            orderEntrust.setCurrentPrice(currentPrice);
            orderEntrust.setIsBlast(0); 
            orderEntrust.setIsFromSpot(0);
            orderEntrust.setPrincipalAmount(wallet.getCoinSellPrincipalAmount()); 

            memberContractWalletService.freezeCoinSellPosition(wallet.getId(), volume);

            
            orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_ING); 
            boolean retObj = contractOrderEntrustService.save(orderEntrust);

            if (retObj) {
                
                rocketMQTemplate.convertAndSend("swap-coin-order-close", JSON.toJSONString(orderEntrust));

                
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("symbol", contractCoin.getSymbol());
                jsonObj.put("walletId", wallet.getId());
                rocketMQTemplate.convertAndSend("member-coin-wallet-change", JSON.toJSONString(jsonObj));

                
                
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

        return MessageResult.error(msService.getMessage("UNABLE_CLOSE_POSITION"));
    }



    
    @ApiOperation(value = "Cancel contract order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entrustId", value = "Entrust ID"),
    })
    @PermissionOperation
    @RequestMapping("cancel")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                     Long entrustId
    ) {
        ContractOrderEntrustCoin entrustOrder = contractOrderEntrustService.getById(entrustId);
        if(entrustOrder == null) {
            return MessageResult.error(500, msService.getMessage("ORDER_NO_EXIST"));
        }
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(cancelRedisKey,user.getId(),entrustId);
        String redisVal = ops.get(key);
        if(redisVal!=null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key,"11",3, TimeUnit.MINUTES);

        Member member = memberFeign.findMemberById(user.getId());
        if (member == null){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        if(!entrustOrder.getMemberId().equals(member.getId())) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ILLEGAL_OPERATION"));
        }
        if(entrustOrder.getStatus() != ContractOrderEntrustStatus.ENTRUST_ING) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("DELEGATE_STATUS_ERROR"));
        }

        
        rocketMQTemplate.convertAndSend("swap-coin-order-cancel", JSON.toJSONString(entrustOrder));

        
        
        MessageResult result = MessageResult.success(msService.getMessage("CANCELLATION_SUCCESSFUL"));
        result.setData(entrustOrder);
        redisTemplate.delete(key);
        return result;
    }

    
    @ApiOperation(value = "Cancel contract orders (cancel all: limit + plan + market)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("cancel-all")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelAllOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                        Long contractCoinId
    ) {
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(cancelAllRedisKey,user.getId(),contractCoinId);
        String redisVal = ops.get(key);
        if(redisVal!=null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key,"11",3, TimeUnit.MINUTES);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        List<ContractOrderEntrustCoin> orderList = contractOrderEntrustService.list(new QueryWrapper<ContractOrderEntrustCoin>().eq("member_id",member.getId()).eq("contract_id",contractCoinId));
        if(orderList != null && orderList.size() > 0) {
            for (int i = 0; i < orderList.size(); i++) {
                ContractOrderEntrustCoin entrustOrder = orderList.get(i);
                if (!entrustOrder.getMemberId().equals(member.getId())) {
                    continue;
                }
                if (entrustOrder.getStatus() != ContractOrderEntrustStatus.ENTRUST_ING) {
                    continue;
                }

                
                rocketMQTemplate.convertAndSend("swap-coin-order-cancel", JSON.toJSONString(entrustOrder));
            }
        }
        
        
        MessageResult result = MessageResult.success(msService.getMessage("CANCELLATION_SUCCESSFUL"));
        redisTemplate.delete(key);
        return result;
    }

    
    @ApiOperation(value = "Get current position list")
    @PermissionOperation
    @RequestMapping("position-list")
    public MessageResult positionList(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) {
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        List<MemberContractWalletCoin> list = memberContractWalletService.findAllByMemberId(member.getId());
        if (list == null) {
            return MessageResult.error(500, msService.getMessage("WALLET_NOT_IN"));
        }

        
        for (MemberContractWalletCoin wallet : list) {
            BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(wallet.getContractCoin().getSymbol()).getNowPrice();
            
            BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO;
            
            if (wallet.getCoinBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
                usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(currentPrice.divide(wallet.getCoinBuyPrice(), 4, BigDecimal.ROUND_DOWN).subtract(BigDecimal.ONE).multiply(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition())).multiply(wallet.getCoinShareNumber()));
            }

            
            if (wallet.getCoinSellPrice().compareTo(BigDecimal.ZERO) > 0) {
                usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(BigDecimal.ONE.subtract(currentPrice.divide(wallet.getCoinSellPrice(), 4, BigDecimal.ROUND_DOWN)).multiply(wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition())).multiply(wallet.getCoinShareNumber()));
            }

            wallet.setCoinTotalProfitAndLoss(usdtTotalProfitAndLoss);
        }

        MessageResult result = MessageResult.success("success");
        result.setData(list);
        return result;
    }

    
    @ApiOperation(value = "Get current position details")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("position-detail")
    public MessageResult positionDetail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, Long contractCoinId) {
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) {
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        ContractCoinCoin coin = contractCoinService.getById(contractCoinId);
        if (coin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWalletCoin wallet = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(), coin);
        if (wallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }

        
        BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol()).getNowPrice();

        
        BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO;
        
        if (wallet.getCoinBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
            usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(currentPrice.divide(wallet.getCoinBuyPrice(), 4, BigDecimal.ROUND_DOWN).subtract(BigDecimal.ONE).multiply(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition())).multiply(wallet.getCoinShareNumber()));
        }
        
        if (wallet.getCoinSellPrice().compareTo(BigDecimal.ZERO) > 0) {
            usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(BigDecimal.ONE.subtract(currentPrice.divide(wallet.getCoinSellPrice(), 4, BigDecimal.ROUND_DOWN)).multiply(wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition())).multiply(wallet.getCoinShareNumber()));
        }

        wallet.setCoinTotalProfitAndLoss(usdtTotalProfitAndLoss);

        MessageResult result = MessageResult.success("success");
        result.setData(wallet);
        return result;
    }

    
    @ApiOperation(value = "Get current entrust list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("current")
    public Page<ContractOrderEntrustCoin> entrustList(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                                      Long contractCoinId, 
                                                      int pageNo,
                                                      int pageSize
    ) {
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        IPage<ContractOrderEntrustCoin> contractOrderEntrustOrders = contractOrderEntrustService.queryPageEntrustingOrdersBySymbol(member.getId(), contractCoinId, pageNo, pageSize);
        return IPage2Page(contractOrderEntrustOrders);
    }

    
    @ApiOperation(value = "Get historical entrust list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
    })
    @PermissionOperation
    @RequestMapping("history")
    public Page<ContractOrderEntrustCoin> entrustListHistory(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                                             Long contractCoinId, 
                                                             int pageNo,
                                                             int pageSize
    ) {
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        IPage<ContractOrderEntrustCoin> contractOrderEntrustOrders = contractOrderEntrustService.queryPageEntrustHistoryOrdersBySymbol(member.getId(), contractCoinId, pageNo, pageSize);
        return IPage2Page(contractOrderEntrustOrders);
    }

    
    @ApiOperation(value = "Whether position mode (cross/fixed) can be switched")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "targetPattern", value = "Position mode"),
    })
    @PermissionOperation
    @RequestMapping("can-switch-pattern")
    public MessageResult canSwitchPattern(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, Long contractCoinId, ContractOrderPattern targetPattern) {
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        ContractCoinCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWalletCoin wallet = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(), contractCoin);
        if (wallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }

        ContractOrderPattern temPattern = targetPattern == ContractOrderPattern.CROSSED ? ContractOrderPattern.FIXED : ContractOrderPattern.CROSSED;
        
        long sizeEntrustOrder = contractOrderEntrustService.queryEntrustingOrdersCountByContractCoinIdAndPattern(member.getId(), contractCoinId, temPattern);
        if (sizeEntrustOrder > 0) {
            return MessageResult.error(500, msService.getMessage("CLOSE_WITHDRAW"));
        }
        MessageResult result = MessageResult.success("success");
        result.setData(null);
        return result;
    }

    
    @ApiOperation(value = "Change position mode")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "targetPattern", value = "Position mode"),
    })
    @PermissionOperation
    @RequestMapping("switch-pattern")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult switchPattern(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, Long contractCoinId, ContractOrderPattern targetPattern) {
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        ContractCoinCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWalletCoin memberContractWallet = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(), contractCoin);
        if (memberContractWallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }
        if(memberContractWallet.getCoinBuyPosition().add(memberContractWallet.getCoinFrozenBuyPosition()).add(memberContractWallet.getCoinSellPosition()).add(memberContractWallet.getCoinFrozenSellPosition()).compareTo(BigDecimal.ZERO)>0){
            return MessageResult.error(500, msService.getMessage("CLOSE_WITHDRAW"));
        }

        if (targetPattern != ContractOrderPattern.FIXED && targetPattern != ContractOrderPattern.CROSSED) {
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }
        ContractOrderPattern temPattern = targetPattern == ContractOrderPattern.CROSSED ? ContractOrderPattern.FIXED : ContractOrderPattern.CROSSED;
        
        long sizeEntrustOrder = contractOrderEntrustService.queryEntrustingOrdersCountByContractCoinIdAndPattern(member.getId(), contractCoinId, temPattern);
        if (sizeEntrustOrder > 0) {
            return MessageResult.error(500, msService.getMessage("CANCEL_FIRST"));
        }

        memberContractWallet.setCoinPattern(targetPattern);
        boolean save = memberContractWalletService.saveOrUpdate(memberContractWallet);

        if (save) {

            
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", memberContractWallet.getId());
            rocketMQTemplate.convertAndSend("member-coin-wallet-change", JSON.toJSONString(jsonObj));

            MessageResult result = MessageResult.success(msService.getMessage("POSITION_MODE1"));
            result.setData(memberContractWallet);
            return result;
        } else {
            return MessageResult.error(500, msService.getMessage("POSITION_MODE2"));
        }
    }

    
    @ApiOperation(value = "Modify leverage for the specified trading pair")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "leverage", value = "Entrust price"),
            @ApiImplicitParam(name = "direction", value = "Direction"),
    })
    @PermissionOperation
    @RequestMapping("modify-leverage")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult modifyLeverage(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                        Long contractCoinId,
                                        BigDecimal leverage,
                                        ContractOrderDirection direction) {
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        ContractCoinCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWalletCoin memberContractWallet = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(), contractCoin);
        if (memberContractWallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }

        
        if (contractCoin.getLeverageType() == 1) { 
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
        } else { 
            String[] leverageArr = contractCoin.getLeverage().split(",");
            if (leverageArr.length != 2) return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_ERROR"));

            BigDecimal low = BigDecimal.valueOf(Integer.parseInt(leverageArr[0]));
            BigDecimal high = BigDecimal.valueOf(Integer.parseInt(leverageArr[1]));
            if (leverage.compareTo(low) < 0 || leverage.compareTo(high) > 0) {
                return MessageResult.error(500, msService.getMessage("CONTRACT_MULTIPLE_NOT_ALLOWED"));
            }
        }

        
        long sizeEntrustOrder = contractOrderEntrustService.queryEntrustingOrdersCountByContractCoinId(member.getId(), contractCoinId);
        if (sizeEntrustOrder > 0) {
            return MessageResult.error(500, msService.getMessage("CANCEL_FIRST"));
        }

        
        if (memberContractWallet.getCoinPattern() == ContractOrderPattern.FIXED) {
            if (direction == ContractOrderDirection.BUY) { 
                if (leverage.compareTo(memberContractWallet.getCoinBuyLeverage()) > 0) { 
                    
                    BigDecimal needPrinAmount = memberContractWallet.getCoinBuyPosition().multiply(memberContractWallet.getCoinShareNumber()).divide(leverage, 8, BigDecimal.ROUND_DOWN);
                    if (needPrinAmount.compareTo(memberContractWallet.getCoinBuyPrincipalAmount()) > 0) {
                        
                        if (memberContractWallet.getCoinBalance().compareTo(needPrinAmount.subtract(memberContractWallet.getCoinBuyPrincipalAmount())) < 0) {
                            return MessageResult.error(500, msService.getMessage("INSUFFICIENT_MARGIN"));
                        }
                        
                        memberContractWalletService.increaseCoinBuyPrincipalAmount(memberContractWallet.getId(), needPrinAmount.subtract(memberContractWallet.getCoinBuyPrincipalAmount()));
                    }
                }
                
                
                memberContractWalletService.modifyCoinBuyLeverage(memberContractWallet.getId(), leverage);
            } else { 
                if (leverage.compareTo(memberContractWallet.getCoinSellLeverage()) > 0) { 
                    
                    BigDecimal needPrinAmount = memberContractWallet.getCoinSellPosition().multiply(memberContractWallet.getCoinShareNumber()).divide(leverage, 8, BigDecimal.ROUND_DOWN);
                    if (needPrinAmount.compareTo(memberContractWallet.getCoinSellPrincipalAmount()) > 0) {
                        
                        if (memberContractWallet.getCoinBalance().compareTo(needPrinAmount.subtract(memberContractWallet.getCoinSellPrincipalAmount())) < 0) {
                            return MessageResult.error(500, msService.getMessage("INSUFFICIENT_MARGIN"));
                        }
                        
                        memberContractWalletService.increaseCoinSellPrincipalAmount(memberContractWallet.getId(), needPrinAmount.subtract(memberContractWallet.getCoinSellPrincipalAmount()));
                    }
                }
                
                
                memberContractWalletService.modifyCoinSellLeverage(memberContractWallet.getId(), leverage);
            }
            
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", memberContractWallet.getId());
            rocketMQTemplate.convertAndSend("member-coin-wallet-change", JSON.toJSONString(jsonObj));
        } else {
            BigDecimal totalNeedPrin = BigDecimal.ZERO;
            BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(contractCoin.getSymbol()).getNowPrice();
            if (direction == ContractOrderDirection.BUY) { 
                totalNeedPrin = totalNeedPrin.add(memberContractWallet.getCoinBuyPosition().multiply(memberContractWallet.getCoinShareNumber()).divide(leverage, 8, BigDecimal.ROUND_DOWN));
            } else {
                totalNeedPrin = totalNeedPrin.add(memberContractWallet.getCoinSellPosition().multiply(memberContractWallet.getCoinShareNumber()).divide(leverage, 8, BigDecimal.ROUND_DOWN));
            }

            
            
            BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO;
            
            if (memberContractWallet.getCoinBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
                usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(currentPrice.divide(memberContractWallet.getCoinBuyPrice(), 8, BigDecimal.ROUND_DOWN).subtract(BigDecimal.ONE).multiply(memberContractWallet.getCoinBuyPosition()).multiply(memberContractWallet.getCoinShareNumber()));
            }
            
            if (memberContractWallet.getCoinSellPrice().compareTo(BigDecimal.ZERO) > 0) {
                usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(BigDecimal.ONE.subtract(currentPrice.divide(memberContractWallet.getCoinSellPrice(), 8, BigDecimal.ROUND_DOWN)).multiply(memberContractWallet.getCoinSellPosition()).multiply(memberContractWallet.getCoinShareNumber()));
            }

            
            if (totalNeedPrin.compareTo(usdtTotalProfitAndLoss.add(memberContractWallet.getCoinBalance()).add(memberContractWallet.getCoinBuyPrincipalAmount()).add(memberContractWallet.getCoinSellPrincipalAmount())) > 0) {
                return MessageResult.error(500, msService.getMessage("INSUFFICIENT_MARGIN"));
            }

            
            if (direction == ContractOrderDirection.BUY) {
                memberContractWalletService.modifyCoinBuyLeverage(memberContractWallet.getId(), leverage);
            }else {
                memberContractWalletService.modifyCoinSellLeverage(memberContractWallet.getId(), leverage);
            }
            
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("symbol", contractCoin.getSymbol());
            jsonObj.put("walletId", memberContractWallet.getId());
            rocketMQTemplate.convertAndSend("member-coin-wallet-change", JSON.toJSONString(jsonObj));
        }

        MessageResult result = MessageResult.success("success");
        result.setData(null);
        return result;
    }

    
    @ApiOperation(value = "Adjust margin")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "contractCoinId", value = "Contract trading pair"),
            @ApiImplicitParam(name = "principal", value = "Margin"),
            @ApiImplicitParam(name = "direction", value = "Direction"),
            @ApiImplicitParam(name = "type", value = "0: Increase  1: Decrease"),
    })
    @PermissionOperation
    @RequestMapping("ajust-principal")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult ajustPrincipal(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                        Long contractCoinId,
                                        BigDecimal principal,
                                        ContractOrderDirection direction,
                                        Integer type) {
        
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member member = memberFeign.findMemberById(user.getId());
        if (member == null) return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        ContractCoinCoin contractCoin = contractCoinService.getById(contractCoinId);
        if (contractCoin == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_NOT_EXIST"));
        }
        MemberContractWalletCoin memberContractWallet = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(), contractCoin);
        if (memberContractWallet == null) {
            return MessageResult.error(500, msService.getMessage("CONTRACT_ACCOUNT_NOT_EXIST"));
        }
        if (memberContractWallet.getCoinPattern() == ContractOrderPattern.CROSSED) {
            return MessageResult.error(500, msService.getMessage("FULL_WAREHOUSE_MODE"));
        }

        
        BigDecimal currentPrice = contractCoinMatchFactory.getContractCoinMatch(contractCoin.getSymbol()).getNowPrice();
        if (direction == ContractOrderDirection.BUY) { 
            if (type == 1) { 
                
                BigDecimal pL = currentPrice.divide(memberContractWallet.getCoinBuyPrice(), 8, BigDecimal.ROUND_DOWN).subtract(BigDecimal.ONE).multiply(memberContractWallet.getCoinBuyPosition()).multiply(memberContractWallet.getCoinShareNumber());
                
                if (memberContractWallet.getCoinBuyPrincipalAmount().subtract(principal).compareTo(memberContractWallet.getCoinBuyPrincipalAmount().add(pL)) < 0) {
                    return MessageResult.error(500, msService.getMessage("INSUFFICIENT_MARGIN_UNABLE_ADJUST"));
                } else {
                    memberContractWalletService.decreaseCoinBuyPrincipalAmount(memberContractWallet.getId(), principal);
                    return MessageResult.success("success");
                }
            } else { 
                if (memberContractWallet.getCoinBalance().compareTo(principal) < 0) {
                    return MessageResult.error(500, msService.getMessage("INSUFFICIENT_MARGIN_UNABLE_ADJUST"));
                }
                memberContractWalletService.increaseCoinBuyPrincipalAmount(memberContractWallet.getId(), principal);
                return MessageResult.success("success");
            }
        } else {
            if (type == 1) { 
                
                BigDecimal pL = BigDecimal.ONE.subtract(currentPrice.divide(memberContractWallet.getCoinSellPrice(), 8, BigDecimal.ROUND_DOWN)).multiply(memberContractWallet.getCoinSellPosition()).multiply(memberContractWallet.getCoinShareNumber());
                
                if (memberContractWallet.getCoinSellPrincipalAmount().subtract(principal).compareTo(memberContractWallet.getCoinSellPrincipalAmount().add(pL)) < 0) {
                    return MessageResult.error(500, msService.getMessage("INSUFFICIENT_MARGIN_UNABLE_ADJUST"));
                } else {
                    memberContractWalletService.decreaseCoinSellPrincipalAmount(memberContractWallet.getId(), principal);
                    return MessageResult.success("success");
                }
            } else { 
                if (memberContractWallet.getCoinBalance().compareTo(principal) < 0) {
                    return MessageResult.error(500, msService.getMessage("INSUFFICIENT_MARGIN_UNABLE_ADJUST"));
                }
                memberContractWalletService.increaseCoinSellPrincipalAmount(memberContractWallet.getId(), principal);
                return MessageResult.success("success");
            }
        }
    }

}

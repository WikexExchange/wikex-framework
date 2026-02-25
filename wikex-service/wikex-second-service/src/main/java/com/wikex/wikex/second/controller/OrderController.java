package com.wikex.wikex.second.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.ContractOptionOrderScreen;
import com.wikex.wikex.screen.ContractSecondOrderScreen;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.engine.ContractCoinMatch;
import com.wikex.wikex.second.engine.ContractCoinMatchFactory;
import com.wikex.wikex.second.entity.ContractSecondCycle;
import com.wikex.wikex.second.entity.ContractSecondOrder;
import com.wikex.wikex.second.entity.ContractSecondSet;
import com.wikex.wikex.second.service.ContractSecondCycleService;
import com.wikex.wikex.second.service.ContractSecondOrderService;
import com.wikex.wikex.second.service.ContractSecondSetService;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberSecondWallet;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberSecondWalletFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Entrusted order processing class
 */
@Api(tags = "Entrusted Order Processing")
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController extends BaseController {
    @Autowired
    private ContractSecondSetService contractSecondSetService;
    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private MemberFeign memberService;

    @Autowired
    private ContractSecondCycleService contractSecondCycleService;

    @Autowired
    private ContractSecondOrderService contractSecondOrderService;

    @Autowired
    private MemberSecondWalletFeign memberSecondWalletService;

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private String openRedisKey = "SECOND_OPEN_%s_%s";


    @ApiOperation(value = "Add Order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "direction", value = "Direction 1: Bullish  2: Bearish"),
            @ApiImplicitParam(name = "optionId", value = "Participation object"),
            @ApiImplicitParam(name = "amount", value = "Participation amount"),
    })
    @RequestMapping("add")
    @Transactional
    @PermissionOperation
    public MessageResult addOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                  String symbol, // Trading pair symbol
                                  String coinSymbol, // Trading coin
//                                   String baseSymbol,// Trading base coin
                                  ContractSecondOrderDirection direction, // 0: Bullish  1: Bearish
                                  Long cycleId, // Cycle ID
                                  BigDecimal amount // Participation amount
    ) {
        Assert.notNull(symbol, msService.getMessage("SELECT_SYMBOL"));
        Assert.notNull(direction, msService.getMessage("CHOOSE_UP_OR_DOWN"));
        Assert.notNull(cycleId, msService.getMessage("SELECT_CONTRACT_PERIOD"));
        Assert.notNull(amount, msService.getMessage("INPUT_AMOUNT"));
        AuthMember user = AuthMember.toAuthMember(authMember);
        // Acquire redis lock
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(openRedisKey,user.getId(),symbol);
        String redisVal = ops.get(key);
        if(redisVal!=null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key,"11",3, TimeUnit.MINUTES); // 3 minutes
        Member member = memberService.findMemberById(user.getId());
        if (member == null){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ACCOUNT_NOT_EXIST"));
        }
        // Whether the user is prohibited from trading
        if(member.getTransactionStatus().equals(BooleanEnum.IS_FALSE.getCode())){
            redisTemplate.delete(key);
            return MessageResult.error(500,msService.getMessage("CANNOT_TRADE"));
        }

        // Prediction direction must be either bullish or bearish
        if(direction != ContractSecondOrderDirection.BUY && direction != ContractSecondOrderDirection.SELL) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }

        // Get settings
        ContractSecondCycle cycle = contractSecondCycleService.findOne(cycleId);
        if(cycle==null){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }

        // Whether the stake amount is out of range
        if(amount.compareTo(cycle.getMinAmount()) < 0 || amount.compareTo(cycle.getMaxAmount()) > 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("AMOUNT_OUT_OF_RANGE"));
        }
        // Get options contract wallet
        MemberSecondWallet memberSecondWallet = memberSecondWalletService.findByCoinUnitAndMemberId(coinSymbol, member.getId());
        if(memberSecondWallet==null){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("BALANCE_NOT_ENOUGH"));
        }
        // Check order quantity and direction
        List<ContractSecondOrder> list = contractSecondOrderService.findByMemberIdAndSymbolAndStatus(member.getId(),symbol, ContractSecondOrderStatus.OPEN);
        if(list.size()>1){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SWAP_FAILED"));
        }else if(list.size()==1){
            ContractSecondOrder order = list.get(0);
            if(!order.getDirection().equals(direction)){
                redisTemplate.delete(key);
                return MessageResult.error(500, msService.getMessage("SWAP_FAILED"));
            }
        }


        // Get compensation settings by time period
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String h = sdf.format(new Date());
        ContractSecondSet set = contractSecondSetService.findSetByTime(h);
        ContractSecondOrderType type = ContractSecondOrderType.YES;
        if(set!=null){
            SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String day = sdfDay.format(new Date());
            try {
                Date start = sdfTime.parse(day + " "+set.getStartTime());
                Date end = sdfTime.parse(day + " "+set.getEndTime());
                Integer count = contractSecondOrderService.countOrderByTime(member.getId(),start,end);
                if(count!=null && count.intValue()>=set.getOrderNum()){
                    type = ContractSecondOrderType.NO;
                }
                if(memberSecondWallet.getBalance().multiply(set.getLimitRate()).compareTo(amount)==-1){
                    type = ContractSecondOrderType.NO;
                }
            } catch (ParseException e) {
                redisTemplate.delete(key);
                e.printStackTrace();
            }

        }else {
            type = ContractSecondOrderType.NO;
        }
        BigDecimal fee = BigDecimal.ZERO;
        // Requires stake amount + fee
        BigDecimal totalAmount = amount.add(fee);
        if(totalAmount.compareTo(memberSecondWallet.getBalance()) > 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("BALANCE_NOT_ENOUGH"));
        }
        // Get latest price
        BigDecimal openPrice = BigDecimal.ZERO;
        ContractCoinMatch match = contractCoinMatchFactory.getContractCoinMatch(symbol);
        if (match != null) {
            openPrice = match.getNowPrice();
        }

        // Create new order
        ContractSecondOrder orderObj = new ContractSecondOrder();
//        orderObj.setBaseSymbol(baseSymbol);
        orderObj.setBetAmount(amount);
        orderObj.setCoinSymbol(coinSymbol);
        orderObj.setSymbol(symbol);
        orderObj.setDirection(direction);
        orderObj.setFee(fee);
        orderObj.setMemberId(member.getId());
        orderObj.setCycleId(cycleId);
        orderObj.setCycleRate(cycle.getCycleRate());
        orderObj.setCycleLength(cycle.getCycleLength());
        orderObj.setOpenPrice(openPrice);
        orderObj.setType(type);
        orderObj.setResult(ContractSecondOrderResult.WAIT);
        orderObj.setStatus(ContractSecondOrderStatus.OPEN);
        long currentTime = System.currentTimeMillis() ;
        currentTime +=(cycle.getCycleLength()*1000);
        orderObj.setCloseTime(new Date(currentTime));
        orderObj.setCreateTime(new Date());
        orderObj.setUpdateTime(new Date());
        contractSecondOrderService.save(orderObj);
        // Lock assets
        memberSecondWalletService.freezeBalance(memberSecondWallet.getId(), amount.add(fee));
        redisTemplate.delete(key);
        return MessageResult.success(msService.getMessage("ADDORDER_SUCCESSFL_PARTICIPATED"));
    }

    /**
     * Get current position records for the trading pair
     * @param authMember
     * @param symbol
     * @return
     */
    @ApiOperation(value = "Get participation records for the current currency with specified period ID")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "optionId", value = "Participation object"),
    })
    @PermissionOperation
    @RequestMapping("current")
    public MessageResult current(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                 String symbol // Trading pair symbol
    ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        List<ContractSecondOrder> orderList = contractSecondOrderService.findOpeningOrders(user.getId(), symbol);
        MessageResult result = MessageResult.success();
        result.setData(orderList);
        return result;
    }

    /**
     * Get historical participation records for current currency
     * @param authMember
     * @param symbol
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "Get historical participation records for the current currency")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("history")
    @PermissionOperation
    public Page<ContractSecondOrder> history(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                             @RequestParam(value = "symbol" ,required = false) String symbol,
                                             @RequestParam(value = "pageNo" ,required = false) int pageNo,
                                             @RequestParam(value = "pageSize" ,required = false) int pageSize
    ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ContractSecondOrder> list = contractSecondOrderService.findAll(user.getId(), symbol, pageNo, pageSize);
        return IPage2Page(list);
    }

    /**
     * Get cycles
     * @return
     */

    @RequestMapping("cycles")
    public MessageResult cycles(){
        List<ContractSecondCycle> list = contractSecondCycleService.list();
        MessageResult result = MessageResult.success();
        result.setData(list);
        return result;
    }

    /**
     * Get supported tradable coins
     * @return
     */
    @ApiOperation(value = "Get supported tradable coins")
    @RequestMapping("coins")
    public MessageResult coins(){
        MessageResult result = MessageResult.success();
        List<String> list = new ArrayList<>();
        list.add("USDT");
        list.add("BTC");
        list.add("ETH");
        list.add("TRX");
        result.setData(list);
        return result;
    }

    /**
     * Get current compensation settings
     * @return
     */
    @ApiOperation(value = "Get current compensation settings")
    @RequestMapping("getAllSets")
    public MessageResult getAllSets() {
        List<ContractSecondSet> set = contractSecondSetService.list();
        MessageResult result = MessageResult.success();
        result.setData(set);
        return result;
    }

}

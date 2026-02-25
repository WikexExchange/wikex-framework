package com.wikex.wikex.option.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.option.entity.ContractOption;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.entity.ContractOptionOrder;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.wikex.wikex.option.service.ContractOptionOrderService;
import com.wikex.wikex.option.service.ContractOptionService;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
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
    private LocaleMessageSourceService msService;

    @Autowired
    private MemberFeign memberService;

    @Autowired
    private ContractOptionCoinService contractOptionCoinService;

    @Autowired
    private ContractOptionOrderService contractOptionOrderService;

    @Autowired
    private ContractOptionService contractOptionService;

    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private String openRedisKey = "OPTION_OPEN_%s_%s";

    @ApiOperation(value = "Add order")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "direction", value = "Direction 1: Bullish  2: Bearish"),
            @ApiImplicitParam(name = "optionId", value = "Participation object"),
            @ApiImplicitParam(name = "amount", value = "Participation amount"),
    })
    @PermissionOperation
    @RequestMapping("add")
    @Transactional
    public MessageResult addOrder(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                  String symbol, // Trading pair symbol
                                  ContractOptionOrderDirection direction, // 1: Bullish  2: Bearish
                                  Long optionId, // Participation object
                                  BigDecimal amount // Participation amount
    ) {
        Assert.notNull(symbol, msService.getMessage("SELECT_SYMBOL"));
        Assert.notNull(direction, msService.getMessage("CHOOSE_UP_OR_DOWN"));
        Assert.notNull(optionId, msService.getMessage("SELECT_CONTRACT_PERIOD"));
        Assert.notNull(amount, msService.getMessage("INPUT_AMOUNT"));
        AuthMember user = AuthMember.toAuthMember(authMember);
        // Get redis lock
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String key = String.format(openRedisKey,user.getId(),symbol);
        String redisVal = ops.get(key);
        if(redisVal!=null){
            return MessageResult.error(500, msService.getMessage("PLEASE_WAIT"));
        }
        ops.set(key,"11",3, TimeUnit.MINUTES);// 3 minutes
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

        ContractOptionCoin contractOptionCoin = contractOptionCoinService.findBySymbol(symbol);
        if (contractOptionCoin == null){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("SYMBOL_DOES_NOT_EXIST"));
        }
        // Whether prediction direction is bullish or bearish
        if(direction != ContractOptionOrderDirection.BUY && direction != ContractOptionOrderDirection.SELL) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ILLEGAL_ARGUMENT"));
        }

        if(direction == ContractOptionOrderDirection.SELL && contractOptionCoin.getEnableSell() == BooleanEnum.IS_FALSE.getCode()) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NO_SHORT"));
        }
        if(direction == ContractOptionOrderDirection.BUY && contractOptionCoin.getEnableBuy() == BooleanEnum.IS_FALSE.getCode()) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("NO_LONG"));
        }
        // Whether the option contract exists
        ContractOption contractOption = contractOptionService.findOne(optionId);
        if (contractOption == null){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CONTRACT_PERIOD_DOES_NOT_EXIST"));
        }
        if(contractOption.getStatus() != ContractOptionStatus.STARTING) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("CLOSE_THE_BET"));
        }
        if (!contractOption.getSymbol().equals(symbol)){
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("OPTION_CONTRACT_FAILURE"));
        }

        // Whether the bet amount exceeds the range
        String[] amountArr = contractOptionCoin.getAmount().split(",");
        BigDecimal amountStart = BigDecimal.valueOf(Long.valueOf(amountArr[0]));
        BigDecimal amountEnd = BigDecimal.valueOf(Long.valueOf(amountArr[amountArr.length - 1]));
        if(amount.compareTo(amountStart) < 0 || amount.compareTo(amountEnd) > 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("AMOUNT_OUT_OF_RANGE"));
        }

        // Whether already participated
        List<ContractOptionOrder> contractOptionOrderList = contractOptionOrderService.findByMemberIdAndOptionId(member.getId(), optionId);
        if(contractOptionOrderList != null && contractOptionOrderList.size() > 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("ADDORDER_PARTICIPATED_ERROR"));
        }

        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(contractOptionCoin.getBaseSymbol(), member.getId());
        // Need bet amount + fee
        BigDecimal totalAmount = amount.add(contractOptionCoin.getFeePercent().multiply(amount));
        if(totalAmount.compareTo(memberWallet.getBalance()) > 0) {
            redisTemplate.delete(key);
            return MessageResult.error(500, msService.getMessage("BALANCE_NOT_ENOUGH"));
        }

        // Create new order
        ContractOptionOrder orderObj = new ContractOptionOrder();
        orderObj.setBaseSymbol(contractOptionCoin.getBaseSymbol());
        orderObj.setBetAmount(amount);
        orderObj.setCoinSymbol(contractOptionCoin.getCoinSymbol());
        orderObj.setOptionId(contractOption.getId());
        orderObj.setDirection(direction);
        orderObj.setFee(contractOptionCoin.getFeePercent().multiply(amount));
        orderObj.setWinFee(BigDecimal.ZERO);
        orderObj.setMemberId(member.getId());
        orderObj.setResult(ContractOptionOrderResult.WAIT);
        orderObj.setRewardAmount(BigDecimal.ZERO);
        orderObj.setStatus(ContractOptionOrderStatus.OPEN);
        orderObj.setSymbol(contractOptionCoin.getSymbol());
        orderObj.setCreateTime(Calendar.getInstance().getTimeInMillis());
        orderObj.setOptionNo(contractOption.getOptionNo());
        contractOptionOrderService.save(orderObj);

        // Lock assets
        memberWalletService.freezeBalance(memberWallet.getId(), amount.add(amount.multiply(contractOptionCoin.getFeePercent())));

        // Increase total bet amount
        if(direction == ContractOptionOrderDirection.BUY) {
            contractOption.setTotalBuy(contractOption.getTotalBuy().add(amount));
            contractOption.setTotalBuyCount(contractOption.getTotalBuyCount() + 1);
        }else {
            contractOption.setTotalSell(contractOption.getTotalSell().add(amount));
            contractOption.setTotalSellCount(contractOption.getTotalSellCount() + 1);
        }
        contractOptionService.saveOrUpdate(contractOption);
        redisTemplate.delete(key);
        return MessageResult.success(msService.getMessage("ADDORDER_SUCCESSFL_PARTICIPATED"));
    }

    /**
     * Get participation records for the specified period ID of the current currency
     * @param authMember
     * @param symbol
     * @param optionId
     * @return
     */
    @ApiOperation(value = "Get participation records for the specified period ID of the current currency")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "optionId", value = "Participation object"),
    })
    @PermissionOperation
    @RequestMapping("current")
    public MessageResult current(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                 String symbol, // Trading pair symbol
                                 Long optionId // Participation object
    ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        List<ContractOptionOrder> orderList = contractOptionOrderService.findByMemberIdAndOptionId(user.getId(), optionId);
        MessageResult result = MessageResult.success();
        result.setData(orderList);
        return result;
    }

    /**
     * Get historical participation records of the current currency
     * @param authMember
     * @param symbol
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "Get historical participation records of the current currency")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @PermissionOperation
    @RequestMapping("history")
    public MessageResult history(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                 @RequestParam(value = "symbol" ,required = false) String symbol,
                                 @RequestParam(value = "pageNo" ,required = false) int pageNo,
                                 @RequestParam(value = "pageSize" ,required = false) int pageSize
    ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Page<ContractOptionOrder> list = contractOptionOrderService.findAll(user.getId(), symbol, pageNo, pageSize);
        MessageResult result = MessageResult.success();
        result.setData(IPage2Page(list));
        return result;
    }
}

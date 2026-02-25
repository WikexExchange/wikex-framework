package com.wikex.wikex.user.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.ConvertCoin;
import com.wikex.wikex.user.entity.ConvertOrder;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.service.ConvertCoinService;
import com.wikex.wikex.user.service.ConvertOrderService;
import com.wikex.wikex.user.service.MemberTransactionService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.util.DBUtils;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Api(tags = "Dynamic Coin Conversion")
@RestController
@RequestMapping("convert")
public class ConvertController extends BaseController {

    @Autowired
    private MemberWalletService walletService;

    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private ConvertCoinService coinService;

    @Autowired
    private ConvertOrderService convertOrderService;

    @Autowired
    private DBUtils dbUtils;

    @Autowired
    private MarketFeign marketFeign;

    @Autowired
    private LocaleMessageSourceService mService;

    @ApiOperation(value = "Order list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "Page number"),
            @ApiImplicitParam(name = "pageSize", value = "Page size"),
    })
    @PermissionOperation
    @RequestMapping("orderList")
    public MessageResult findTransaction(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            HttpServletRequest request, int pageNo, int pageSize) throws ParseException {
        // Validate sign-in activity, coin, member, and wallet
        AuthMember member = AuthMember.toAuthMember(authMember);
        MessageResult mr = new MessageResult();
        mr.setCode(0);
        mr.setMessage("success");
        mr.setData(IPage2Page(convertOrderService.queryByMember(member.getId(), pageNo, pageSize)));
        return mr;
    }

    @ApiOperation(value = "Get all coins")
    @RequestMapping("getCoins")
    public MessageResult getCoins() {
        List<ConvertCoin> all = coinService.findByStatus(1);
        return success(all);
    }

    @ApiOperation(value = "Get coins by unit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "Unit"),
    })
    @RequestMapping("getCoinsByUnit")
    public MessageResult getCoinsByUnit(String unit) {
        List<ConvertCoin> all = coinService.findByStatus(1);
        for (int i = all.size() - 1; i >= 0; i--) {
            ConvertCoin convertCoin = all.get(i);
            if (convertCoin.getCoinUnit().equalsIgnoreCase(unit)) {
                all.remove(convertCoin);
            }
        }
        return success(all);
    }

    @ApiOperation(value = "Get price")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromUnit", value = "Source coin"),
            @ApiImplicitParam(name = "toUnit", value = "Target coin"),
    })
    @RequestMapping("getPrice")
    public MessageResult getPrice(String fromUnit, String toUnit) {
        BigDecimal lastPrice = getLastPrice(fromUnit, toUnit);
        if (lastPrice != null) {
            return success(lastPrice);
        }
        return error(mService.getMessage("TRADING_PAIR_NOT_SUPPORTED"));
    }

    private BigDecimal getLastPrice(String fromUnit, String toUnit) {
        List<CoinThumb> thumbList = marketFeign.findSymbolThumb4Feign();
        if (fromUnit.indexOf("#") > 0) {
            dbUtils.excuteUpdateSql(fromUnit.split("#")[1]);
            fromUnit = fromUnit.split("#")[0];
        }
        String symbol = fromUnit.toUpperCase() + "/" + toUnit.toUpperCase();
        String symbol2 = toUnit.toUpperCase() + "/" + fromUnit.toUpperCase();
        for (CoinThumb coinThumb : thumbList) {
            if (coinThumb.getSymbol().equalsIgnoreCase(symbol)) {
                return coinThumb.getClose();
            }
            if (coinThumb.getSymbol().equalsIgnoreCase(symbol2)) {
                return BigDecimal.ONE.divide(coinThumb.getClose(), 8, RoundingMode.HALF_UP);
            }
        }
        return null;
    }

    @ApiOperation(value = "Convert")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fromUnit", value = "Source coin"),
            @ApiImplicitParam(name = "toUnit", value = "Target coin"),
            @ApiImplicitParam(name = "needAmount", value = "Amount to exchange"),
    })
    @PermissionOperation
    @RequestMapping("do-exchange")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult doExchagne(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String fromUnit,
            String toUnit, BigDecimal needAmount) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        ConvertCoin convertCoin = coinService.findByCoinUnit(toUnit);
        // Get last price
        BigDecimal lastPrice = getLastPrice(fromUnit, toUnit);
        if (lastPrice == null) {
            return error(mService.getMessage("TRADING_PAIR_NOT_SUPPORTED"));
        }
        // Check balance
        MemberWallet needMW = walletService.findByCoinUnitAndMemberId(fromUnit, member.getId());
        if (needMW.getBalance().compareTo(needAmount) < 0) {
            return error(mService.getMessage("INSUFFICIENT_BALANCE"));
        }
        // Execute conversion
        MemberWallet targetMW = walletService.findByCoinUnitAndMemberId(toUnit, member.getId());
        BigDecimal amount = needAmount.multiply(lastPrice);

        BigDecimal fee = amount.multiply(convertCoin.getFee());
        amount = amount.subtract(fee);

        // Create order
        ConvertOrder order = new ConvertOrder();
        order.setFee(fee);
        order.setCreateTime(new Date());
        order.setMemberId(member.getId());
        order.setFromUnit(fromUnit);
        order.setToUnit(toUnit);
        order.setFromAmount(needAmount);
        order.setToAmount(amount);
        order.setPrice(lastPrice);
        order.setStatus(1);
        convertOrderService.save(order);

        // Increase target wallet balance
        walletService.increaseBalance(targetMW.getId(), amount);

        // Decrease source wallet balance
        walletService.decreaseBalance(needMW.getId(), needAmount);

        // Add transaction record (increase)
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(amount);
        memberTransaction.setMemberId(member.getId());
        memberTransaction.setSymbol(toUnit);
        memberTransaction.setType(TransactionType.ACTIVITY_BUY.getCode());
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        transactionService.save(memberTransaction);

        // Add transaction record (decrease)
        MemberTransaction memberTransactionOut = new MemberTransaction();
        memberTransactionOut.setFee(BigDecimal.ZERO);
        memberTransactionOut.setAmount(needAmount);
        memberTransactionOut.setMemberId(member.getId());
        memberTransactionOut.setSymbol(fromUnit);
        memberTransactionOut.setType(TransactionType.ACTIVITY_BUY.getCode());
        memberTransactionOut.setCreateTime(DateUtil.getCurrentDate());
        memberTransactionOut.setRealFee("0");
        memberTransactionOut.setDiscountFee("0");
        transactionService.save(memberTransactionOut);

        return success(mService.getMessage("EXCHANGE_SUCCESSFUL"));
    }
}

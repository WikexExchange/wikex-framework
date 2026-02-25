package com.wikex.wikex.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.system.CoinExchangeFactory;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.MemberTransaction4Front;
import com.wikex.wikex.user.vo.MemberWalletVo;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.wikex.wikex.constant.SysConstant.SESSION_MEMBER;

@Api(tags = "Asset")
@RestController
@RequestMapping("/asset")
@Slf4j
public class AssetController extends BaseController {

    @Autowired
    private MemberWalletService walletService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private MemberTransactionService transactionService;

    @Autowired
    private CoinExchangeFactory coinExchangeFactory;

    @Value("${gcx.match.max-limit:1000}")
    private double gcxMatchMaxLimit;

    @Value("${gcx.match.each-limit:5}")
    private double gcxMatchEachLimit;

    @Autowired
    private QuickExchangeService quickExchangeService;

    @Autowired
    private LocaleMessageSourceService sourceService;

    @Autowired
    private EquitySnapshotService equitySnapshotService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TokenSnapshotService tokenSnapshotService;

    @ApiOperation(value = "User wallet information")
    @PermissionOperation
    @RequestMapping("wallet")
    public MessageResult findWallet(@RequestHeader(SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        List<MemberWallet> wallets = walletService.findAllByMemberId(member.getId());
        List<MemberWalletVo> walletVos = new ArrayList<>();
        wallets.forEach(wallet -> {
            MemberWalletVo vo = new MemberWalletVo();
            BeanUtils.copyProperties(wallet, vo);
            Coin coin = coinService.findByUnit(wallet.getCoinId());
            CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(coin.getUnit());
            if (rate != null) {
                coin.setUsdRate(rate.getUsdRate());
                coin.setCnyRate(rate.getCnyRate());
            }

            if (coin.getUsdRate() == null || coin.getUsdRate().compareTo(BigDecimal.ZERO) == 0) {
                Query query = new Query(Criteria.where("coinId").is(coin.getUnit()));
                Document priceDoc = mongoTemplate.findOne(query, Document.class, "coin_last_prices");

                if (priceDoc != null) {
                    String priceStr = priceDoc.getString("price");
                    if (priceStr != null && !priceStr.isEmpty()) {
                        BigDecimal priceUsd = new BigDecimal(priceStr);
                        coin.setUsdRate(priceUsd);
                    }
                }
            }

            vo.setCoin(coin);
            walletVos.add(vo);
        });
        MessageResult mr = MessageResult.success("success");
        mr.setData(walletVos);
        return mr;
    }

    @ApiOperation(value = "Filtered user token balances")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "filteredNegative", value = "include wallets with balance < 0")
    })
    @PermissionOperation
    @GetMapping("wallet/filtered")
    public MessageResult filteredWallet(@RequestHeader(SESSION_MEMBER) String authMember,
            @RequestParam(value = "filteredNegative", required = false, defaultValue = "false") boolean filterNegative) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        List<MemberWallet> wallets = walletService.findAllByMemberId(member.getId());

        List<MemberWalletVo> filtered = new ArrayList<>();
        for (MemberWallet wallet : wallets) {
            if (wallet.getBalance() == null || wallet.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (!filterNegative && wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                continue;
            }

            MemberWalletVo vo = new MemberWalletVo();
            BeanUtils.copyProperties(wallet, vo);

            Coin coin = coinService.findByUnit(wallet.getCoinId());
            if (coin != null) {
                CoinExchangeFactory.ExchangeRate rate = coinExchangeFactory.get(coin.getUnit());
                if (rate != null) {
                    coin.setUsdRate(rate.getUsdRate());
                    coin.setCnyRate(rate.getCnyRate());
                }
            }
            vo.setCoin(coin);
            filtered.add(vo);
        }

        MessageResult mr = MessageResult.success("success");
        mr.setData(filtered);
        return mr;
    }

    @ApiOperation(value = "Query records of a specific type")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "Page number"),
            @ApiImplicitParam(name = "pageSize", value = "Page size"),
            @ApiImplicitParam(name = "type", value = "Transaction type"),
    })
    @PermissionOperation
    @RequestMapping("transaction")
    public MessageResult findTransaction(@RequestHeader(SESSION_MEMBER) String authMember, int pageNo, int pageSize,
            TransactionType type) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        MessageResult mr = new MessageResult();
        mr.setData(IPage2Page(transactionService.queryByMember(member.getId(), pageNo, pageSize, type)));
        mr.setCode(0);
        mr.setMessage("success");
        return mr;
    }

    @ApiOperation(value = "Query all records")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "Page number"),
            @ApiImplicitParam(name = "pageSize", value = "Page size"),
            @ApiImplicitParam(name = "startTime", value = "Start time"),
            @ApiImplicitParam(name = "endTime", value = "End time"),
            @ApiImplicitParam(name = "symbol", value = "Symbol"),
            @ApiImplicitParam(name = "type", value = "Transaction type"),
    })
    @PermissionOperation
    @RequestMapping("transaction/all")
    public MessageResult findTransaction(@RequestHeader(SESSION_MEMBER) String authMember, HttpServletRequest request,
            int pageNo, int pageSize,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "symbol", required = false) String symbol,
            @RequestParam(value = "type", required = false) String type) throws ParseException {
        AuthMember member = AuthMember.toAuthMember(authMember);
        MessageResult mr = new MessageResult();
        TransactionType transactionType = null;
        if (StringUtils.isNotEmpty(type)) {
            transactionType = TransactionType.valueOfOrdinal(Integer.parseInt(type));
        }
        mr.setCode(0);
        mr.setMessage("success");
        Page<MemberTransaction4Front> page = transactionService.queryByMember(member.getId(), pageNo, pageSize,
                transactionType, startTime, endTime, symbol);
        mr.setData(IPage2Page(page));
        return mr;
    }

    @ApiOperation(value = "Find wallet by symbol")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Symbol"),
    })
    @PermissionOperation
    @RequestMapping("wallet/{symbol}")
    public MessageResult findWalletBySymbol(@RequestHeader(SESSION_MEMBER) String authMember,
            @PathVariable String symbol) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        MessageResult mr = MessageResult.success("success");
        mr.setData(walletService.findByCoinUnitAndMemberId(symbol, member.getId()));
        return mr;
    }

    @ApiOperation(value = "Get quick-exchange list")
    @PermissionOperation
    @RequestMapping("wallet/quick-exchange-list")
    public MessageResult queryQuickExchange(@SessionAttribute(SESSION_MEMBER) AuthMember member) {
        List<QuickExchange> retList = quickExchangeService.findAllByMemberId(member.getId());
        MessageResult ret = new MessageResult();
        ret.setCode(0);
        ret.setData(retList);
        ret.setMessage("Success");
        return ret;
    }

    @ApiOperation(value = "Today Token PnL")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Symbol"),
    })
    @PermissionOperation
    @GetMapping("portfolio/pnl/today/{symbol}")
    public MessageResult portfolioPnlTokenToday(@RequestHeader(SESSION_MEMBER) String authMember,
            @PathVariable String symbol) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        Map<String, Object> result = tokenSnapshotService.calculateTodayTokenPnl(memberId, symbol);
        MessageResult mr = MessageResult.success("success");
        mr.setData(result);
        return mr;
    }

    @ApiOperation(value = "Today PnL")
    @PermissionOperation
    @GetMapping("portfolio/pnl/today")
    public MessageResult portfolioPnlToday(@RequestHeader(SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();
        Map<String, Object> result = equitySnapshotService.calculateTodaySnapshot(memberId);

        MessageResult mr = MessageResult.success("success");
        mr.setData(result);
        return mr;
    }

    @ApiOperation(value = "Equity trend")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "range", value = "7d, 30d, 90d, 180d", defaultValue = "7d")
    })
    @PermissionOperation
    @GetMapping("portfolio/equity/trend")
    public MessageResult equityTrend(@RequestHeader(SESSION_MEMBER) String authMember,
            @RequestParam(value = "range", defaultValue = "7d") String range) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        String temp = range.trim().toLowerCase();
        if (temp.endsWith("d")) {
            temp = temp.substring(0, temp.length() - 1);
        }

        int days = Integer.parseInt(temp);
        if (days != 7 && days != 30 && days != 90 && days != 180) {
            return MessageResult.error(sourceService.getMessage("INVALID_RANGE"));
        }

        List<Map<String, Object>> trend = equitySnapshotService.getEquityTrend(memberId, days);
        MessageResult mr = MessageResult.success("success");
        mr.setData(trend);
        return mr;
    }
}

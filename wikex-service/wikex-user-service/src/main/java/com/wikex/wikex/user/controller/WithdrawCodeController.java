package com.wikex.wikex.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.constant.WithdrawStatus;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.exception.InformationExpiredException;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberFundingWallet;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.entity.WithdrawCodeRecord;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.WithdrawWalletInfo;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.checkerframework.checker.units.qual.s;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.wikex.wikex.util.BigDecimalUtils.compare;
import static org.springframework.util.Assert.*;

@Api(tags = "Withdraw Code")
@Slf4j
@RestController
@RequestMapping(value = "/withdrawcode", method = RequestMethod.POST)
public class WithdrawCodeController extends BaseController {

    // @Autowired
    // private MemberAddressService memberAddressService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberFundingWalletService memberFundingWalletService;
    @Autowired
    private WithdrawCodeRecordService withdrawApplyService;
    // @Autowired
    // private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private LocaleMessageSourceService sourceService;
    @Autowired
    private MemberTransactionService memberTransactionService;

    /**
     * Supported withdraw coins
     */
    @RequestMapping("support/coin")
    public MessageResult queryWithdraw() {
        List<Coin> list = coinService.findAllCanWithDraw();
        List<String> list1 = new ArrayList<>();
        list.stream().forEach(x -> list1.add(x.getUnit()));
        MessageResult result = MessageResult.success();
        result.setData(list1);
        return result;
    }

    /**
     * Withdraw coin detailed information
     */
    @ApiOperation(value = "Withdraw coin detailed information")
    @PermissionOperation
    @RequestMapping("support/coin/info")
    public MessageResult queryWithdrawCoin(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        List<Coin> list = coinService.findAllCanWithDraw();

        List<MemberFundingWallet> list1 = memberFundingWalletService.findAllByMemberId(user.getId());
        long id = user.getId();
        List<WithdrawWalletInfo> list2 = list1.stream().filter(x -> {
            for (Coin coin : list) {
                if (coin.getUnit().equals(x.getCoinId())) {
                    return true;
                }
            }
            return false;
        }).map(x -> {
            Coin coin = coinService.findByUnit(x.getCoinId());
            WithdrawWalletInfo walletInfo = WithdrawWalletInfo.builder()
                    .balance(x.getBalance())
                    .withdrawScale(coin.getWithdrawScale())
                    .maxTxFee(coin.getMaxTxFee())
                    .minTxFee(coin.getMinTxFee())
                    .minAmount(coin.getMinWithdrawAmount())
                    .maxAmount(coin.getMaxWithdrawAmount())
                    .name(coin.getName())
                    .nameCn(coin.getNameCn())
                    .threshold(coin.getWithdrawThreshold())
                    .unit(coin.getUnit())
                    // .accountType(coin.getAccountType())
                    .canAutoWithdraw(BooleanEnum.creator(coin.getCanAutoWithdraw()))
                    // .addresses(memberAddressService.queryAddress(id, x.getCoin().getName()))
                    .build();
            return walletInfo;
        }).collect(Collectors.toList());
        MessageResult result = MessageResult.success();
        result.setData(list2);
        return result;
    }

    /**
     * Apply for withdrawal (with verification code check)
     */
    @ApiOperation(value = "Apply for withdrawal (with verification code check)")
    @PermissionOperation
    @RequestMapping("apply/code")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult withdrawCode(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String unit,
            BigDecimal amount, String jyPassword) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(jyPassword, sourceService.getMessage("MISSING_JYPASSWORD"));
        hasText(unit, sourceService.getMessage("MISSING_COIN_TYPE"));
        Coin coin = coinService.findByUnit(unit);
        amount.setScale(coin.getWithdrawScale(), BigDecimal.ROUND_DOWN);
        notNull(coin, sourceService.getMessage("COIN_ILLEGAL"));

        isTrue(coin.getStatus().equals(CommonStatus.NORMAL)
                && coin.getCanWithdraw().equals(BooleanEnum.IS_TRUE.getCode()),
                sourceService.getMessage("COIN_NOT_SUPPORT"));
        isTrue(compare(coin.getMaxWithdrawAmount(), amount),
                sourceService.getMessage("WITHDRAW_MAX") + coin.getMaxWithdrawAmount().toString());
        isTrue(compare(amount, coin.getMinWithdrawAmount()),
                sourceService.getMessage("WITHDRAW_MIN") + coin.getMinWithdrawAmount().toString());

        MemberFundingWallet fundingWallet = memberFundingWalletService.findByCoinUnitAndMemberId(unit, user.getId());
        isTrue(compare(fundingWallet.getBalance(), amount), sourceService.getMessage("INSUFFICIENT_BALANCE"));
        // isTrue(memberAddressService.findByMemberIdAndAddress(user.getId(),
        // address).size() > 0, sourceService.getMessage("WRONG_ADDRESS"));
        // isTrue(memberWallet.getIsLock() == BooleanEnum.IS_FALSE,
        // sourceService.getMessage("WALLET_LOCKED"));
        Member member = memberService.getById(user.getId());
        String mbPassword = member.getJyPassword();
        hasText(mbPassword, sourceService.getMessage("NO_SET_JYPASSWORD"));
        Assert.isTrue(MD5.md5Digest(jyPassword + member.getSalt()).toLowerCase().equals(mbPassword),
                sourceService.getMessage("ERROR_JYPASSWORD"));

        // Freeze user assets
        int result = memberFundingWalletService.freezeBalance(fundingWallet.getId(), amount);
        if (result == 0) {
            throw new InformationExpiredException(sourceService.getMessage("INFORMATION_EXPIRED"));
        }
        // Generate withdraw code record
        WithdrawCodeRecord withdrawApply = new WithdrawCodeRecord();
        withdrawApply.setCoinId(coin.getUnit());
        withdrawApply.setMemberId(user.getId());
        withdrawApply.setWithdrawAmount(amount);
        withdrawApply.setStatus(WithdrawStatus.PROCESSING.getCode());
        // Generate withdraw code (MD5)
        String withdrawCode = MD5.md5Digest(System.currentTimeMillis() + Math.random() + "");
        withdrawApply.setWithdrawCode(withdrawCode);
        withdrawApply.setCreateTime(new Date());
        if (withdrawApplyService.saveOrUpdate(withdrawApply)) {
            MessageResult mr = new MessageResult(0, "success");
            mr.setData(withdrawApply);
            return mr;
        } else {
            throw new InformationExpiredException(sourceService.getMessage("INFORMATION_EXPIRED"));
        }
    }

    /**
     * Withdraw code recharge
     */
    @ApiOperation(value = "Withdraw code recharge")
    @PermissionOperation
    @RequestMapping("apply/recharge")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult withdrawCodeRecharge(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            String withdrawCode) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(withdrawCode, sourceService.getMessage("MISSING_JYPASSWORD"));
        WithdrawCodeRecord record = withdrawApplyService.findByWithdrawCode(withdrawCode);

        if (record != null) {
            if (record.getStatus() != WithdrawStatus.PROCESSING.getCode()) {
                return MessageResult.error(sourceService.getMessage("RECHARGE_CODE_INVALID"));
            }
            withdrawApplyService.withdrawSuccess(record.getId(), user.getId());
            return MessageResult.success(sourceService.getMessage("RECHARGE_CODE_SUCCESS"));
        } else {
            return MessageResult.error(sourceService.getMessage("RECHARGE_CODE_NOT_FOUND"));
        }
    }

    /**
     * Get withdraw code information
     */
    @ApiOperation(value = "Get withdraw code information")
    @PermissionOperation
    @RequestMapping("apply/info")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult getWithdrawCodeInfo(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            String withdrawCode) throws Exception {
        AuthMember user = AuthMember.toAuthMember(authMember);
        hasText(withdrawCode, sourceService.getMessage("MISSING_JYPASSWORD"));
        WithdrawCodeRecord record = withdrawApplyService.findByWithdrawCode(withdrawCode);
        if (record != null) {
            MessageResult ret = new MessageResult(0, "Get success!");
            ret.setData(record);
            return ret;
        } else {
            return MessageResult.error(sourceService.getMessage("RECHARGE_CODE_NOT_FOUND"));
        }
    }

    /**
     * Withdraw code records
     */
    @ApiOperation(value = "Withdraw code records")
    @PermissionOperation
    @RequestMapping("record")
    public MessageResult pageWithdraw(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam("page") Integer page,
            @RequestParam("pageSize") Integer pageSize) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        MessageResult mr = new MessageResult(0, "success");
        IPage<WithdrawCodeRecord> records = withdrawApplyService.findAllByMemberId(user.getId(), page, pageSize);
        mr.setData(IPage2Page(records));
        return mr;
    }
}

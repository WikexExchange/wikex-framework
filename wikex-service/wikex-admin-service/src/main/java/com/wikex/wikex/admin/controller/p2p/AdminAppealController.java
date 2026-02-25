package com.wikex.wikex.admin.controller.p2p;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.exception.InformationExpiredException;
import com.wikex.wikex.p2p.entity.Appeal;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.p2p.feign.AdvertiseFeign;
import com.wikex.wikex.p2p.feign.AppealFeign;
import com.wikex.wikex.p2p.feign.OtcCoinFeign;
import com.wikex.wikex.p2p.feign.OtcOrderFeign;
import com.wikex.wikex.p2p.vo.AppealVo;
import com.wikex.wikex.screen.AppealScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.BigDecimalUtils;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static com.wikex.wikex.util.BigDecimalUtils.add;


 //Backend appeal management
@Slf4j
@RestController
@RequestMapping("/otc/appeal")
public class AdminAppealController extends BaseAdminController {

    @Autowired
    private AppealFeign appealService;

    @Autowired
    private OtcOrderFeign orderService;

    @Autowired
    private AdvertiseFeign advertiseService;

    @Autowired
    private MemberWalletFeign memberWalletService;

    @Autowired
    private MemberFeign memberService;

    @Autowired
    private OtcCoinFeign coinService;

    @Autowired
    private LocaleMessageSourceService msService;

    @Autowired
    private MemberTransactionFeign memberTransactionService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("otc:appeal:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.OTC, operation = "Paginated query backend appeals")
    public MessageResult pageQuery(
            AppealScreen screen) {
        Page page = appealService.appealQuery(screen);
        return success(messageSource.getMessage("GET_SUCCESS"), IPage2Page(page));
    }

    @RequiresPermissions("otc:appeal:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.OTC, operation = "Backend appeal details")
    public MessageResult detail(
            @RequestParam(value = "id") Long id) {
        AppealVo one = appealService.findOneAppealVO(id);
        if (one == null) {
            return error("Data is empty! You should check parameter (id)!");
        }
        return success(one);
    }

    /**
     * Appeal processed – Cancel order
     *
     * @param orderSn order number
     * @return
     * @throws InformationExpiredException
     */
    @RequiresPermissions("otc:appeal:cancel-order")
    @RequestMapping(value = "cancel-order")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult cancelOrder(long appealId, String orderSn, @RequestParam(value = "banned", defaultValue = "false") boolean banned) throws InformationExpiredException {
        Appeal appeal = appealService.findOne(appealId);
        Assert.notNull(appeal, messageSource.getMessage("APPEAL_NOT_FOUND"));
        Long initiatorId = appeal.getInitiatorId();
        Long associateId = appeal.getAssociateId();
        OtcOrder order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = getRet(order, initiatorId, associateId);
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.NONPAYMENT) || order.getStatus().equals(OrderStatus.PAID) || order.getStatus().equals(OrderStatus.APPEAL), msService.getMessage("ORDER_NOT_ALLOW_CANCEL"));
        // Cancel order
        if (!(orderService.cancelOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
        MessageResult result = success("");
        if (ret == 1) {
            // banned = true → disable account
            Member member1 = memberService.findMemberById(initiatorId);
            if (member1.getStatus() == CommonStatus.NORMAL.getCode() && banned) {
                member1.setStatus(CommonStatus.ILLEGAL.getCode());
                memberService.save(member1);
            }
            result = cancel(order, order.getNumber(), associateId);
        } else if (ret == 2) {
            Member member1 = memberService.findMemberById(initiatorId);
            if (member1.getStatus() == CommonStatus.NORMAL.getCode() && banned) {
                member1.setStatus(CommonStatus.ILLEGAL.getCode());
                memberService.save(member1);
            }
            result = cancel(order, add(order.getNumber(), order.getCommission()), associateId);
        } else if (ret == 3) {
            Member member1 = memberService.findMemberById(associateId);
            if (member1.getStatus() == CommonStatus.NORMAL.getCode() && banned) {
                member1.setStatus(CommonStatus.ILLEGAL.getCode());
                memberService.save(member1);
            }
            result = cancel(order, add(order.getNumber(), order.getCommission()), initiatorId);
        } else if (ret == 4) {
            Member member1 = memberService.findMemberById(associateId);
            if (member1.getStatus() == CommonStatus.NORMAL.getCode() && banned) {
                member1.setStatus(CommonStatus.ILLEGAL.getCode());
                memberService.save(member1);
            }
            result = cancel(order, order.getNumber(), initiatorId);
        } else {
            throw new InformationExpiredException("Information Expired");
        }
        appeal.setDealWithTime(DateUtil.getCurrentDate());
        appeal.setIsSuccess(BooleanEnum.IS_FALSE);
        appeal.setStatus(AppealStatus.PROCESSED);
        appealService.updateById(appeal);
        return result;
    }

    private MessageResult cancel(OtcOrder order , BigDecimal amount , Long memberId) throws InformationExpiredException {
        MemberWallet memberWallet;
        // Update advertisement
        if (!advertiseService.updateAdvertiseAmountForCancel(order.getAdvertiseId(), amount)) {
            throw new InformationExpiredException("Information Expired");
        }
        OtcCoin otcCoin = coinService.findOne(order.getCoinId());
        memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), memberId);
        
        MessageResult result = memberWalletService.thawBalance(memberWallet.getCoinId(), memberWallet.getMemberId(), amount);
        if (result.getCode() == 0) {
            return MessageResult.success(messageSource.getMessage("CANCEL_ORDER_SUCCESS"));
        } else {
            throw new InformationExpiredException("Information Expired");
        }
    }

    private int getRet(OtcOrder order, Long initiatorId, Long associateId) {
        int ret = 0;
        if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(initiatorId)) {
            // The appellant is the advertiser and also the payer; seller = associateId
            ret = 1;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(initiatorId)) {
            // The appellant is not the advertiser but is the payer; seller = associateId
            ret = 2;
        } else if (order.getAdvertiseType().equals(AdvertiseType.SELL) && order.getCustomerId().equals(associateId)) {
            // The appellant is the advertiser but not the payer; seller = initiatorId
            ret = 3;
        } else if (order.getAdvertiseType().equals(AdvertiseType.BUY) && order.getMemberId().equals(associateId)) {
            // The appellant is not the advertiser and not the payer; seller = initiatorId
            ret = 4;
        }
        return ret;
    }

    /**
     * Appeal processed – Release order (release coin)
     *
     * @param orderSn order number
     * @return
     */
    @RequiresPermissions("otc:appeal:release-coin")
    @RequestMapping(value = "release-coin")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult confirmRelease(long appealId, String orderSn, @RequestParam(value = "banned", defaultValue = "false") boolean banned) throws Exception {
        Appeal appeal = appealService.findOne(appealId);
        Assert.notNull(appeal, messageSource.getMessage("APPEAL_NOTO_FUND"));
        Long initiatorId = appeal.getInitiatorId();
        Long associateId = appeal.getAssociateId();

        OtcOrder order = orderService.findOneByOrderSn(orderSn);
        notNull(order, msService.getMessage("ORDER_NOT_EXISTS"));
        int ret = getRet(order, initiatorId, associateId);
        isTrue(ret != 0, msService.getMessage("REQUEST_ILLEGAL"));
        isTrue(order.getStatus().equals(OrderStatus.PAID) || order.getStatus().equals(OrderStatus.APPEAL), msService.getMessage("ORDER_STATUS_EXPIRED"));
        if (ret == 1 || ret == 4) {
            // Update advertisement
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), order.getNumber())) {
                throw new InformationExpiredException("Information Expired");
            }
        } else if ((ret == 2 || ret == 3)) {
            // Update advertisement
            if (!advertiseService.updateAdvertiseAmountForRelease(order.getAdvertiseId(), add(order.getNumber(), order.getCommission()))) {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            throw new InformationExpiredException("Information Expired");
        }
        // Release order
        if (!(orderService.releaseOrder(order.getOrderSn()) > 0)) {
            throw new InformationExpiredException("Information Expired");
        }
        OtcCoin otcCoin = coinService.findOne(order.getCoinId());
        // Backend appeal result = release — update wallets of buyer and seller
        this.transferAdmin(otcCoin, order, ret);

        if (ret == 1) {
            generateMemberTransaction(otcCoin, order, TransactionType.OTC_SELL, associateId, BigDecimal.ZERO);
            generateMemberTransaction(otcCoin, order, TransactionType.OTC_BUY, initiatorId, order.getCommission());
        } else if (ret == 2) {
            generateMemberTransaction(otcCoin, order, TransactionType.OTC_SELL, associateId, order.getCommission());
            generateMemberTransaction(otcCoin, order, TransactionType.OTC_BUY, initiatorId, BigDecimal.ZERO);
        } else if (ret == 3) {
            generateMemberTransaction(otcCoin, order, TransactionType.OTC_BUY, associateId, BigDecimal.ZERO);
            generateMemberTransaction(otcCoin, order, TransactionType.OTC_SELL, initiatorId, order.getCommission());
        } else {
            generateMemberTransaction(otcCoin, order, TransactionType.OTC_BUY, associateId, order.getCommission());
            generateMemberTransaction(otcCoin, order, TransactionType.OTC_SELL, initiatorId, BigDecimal.ZERO);
        }
        orderService.onOrderCompleted(order);

        // banned = true → disable account
        if (ret == 1 || ret == 2) {
            Member member1 = memberService.findMemberById(associateId);
            if (member1.getStatus() == CommonStatus.NORMAL.getCode() && banned) {
                member1.setStatus(CommonStatus.ILLEGAL.getCode());
                memberService.save(member1);
            }
        } else {
            Member member1 = memberService.findMemberById(initiatorId);
            if (member1.getStatus() == CommonStatus.NORMAL.getCode() && banned) {
                member1.setStatus(CommonStatus.ILLEGAL.getCode());
                memberService.save(member1);
            }
        }
        appeal.setDealWithTime(DateUtil.getCurrentDate());
        appeal.setIsSuccess(BooleanEnum.IS_TRUE);
        appeal.setStatus(AppealStatus.PROCESSED);
        appealService.updateById(appeal);
        return MessageResult.success(msService.getMessage("SUCCESS"));
    }

    public void transferAdmin(OtcCoin otcCoin, OtcOrder order, int ret) throws InformationExpiredException {
        if (ret == 1 || ret == 4) {
            trancerDetail(otcCoin, order, order.getCustomerId(), order.getMemberId());
        } else {
            trancerDetail(otcCoin, order, order.getMemberId(), order.getCustomerId());
        }
    }

    private void trancerDetail(OtcCoin otcCoin, OtcOrder order, long sellerId, long buyerId) throws InformationExpiredException {
        MemberWallet customerWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), sellerId);
        // Seller and buyer amounts to be processed
        BigDecimal sellerAmount, buyerAmount;
        if (order.getMemberId() == sellerId) {
            sellerAmount = BigDecimalUtils.add(order.getNumber(), order.getCommission());
            buyerAmount = order.getNumber();
        } else {
            sellerAmount = order.getNumber();
            buyerAmount = order.getNumber().subtract(order.getCommission());
        }
        MessageResult is = memberWalletService.decreaseFrozen(customerWallet.getId(), sellerAmount);
        if (is.getCode() == 0) {
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(otcCoin.getUnit(), buyerId);
            MessageResult a = memberWalletService.increaseBalance(memberWallet.getId(), buyerAmount);
            if (a.getCode() != 0) {
                throw new InformationExpiredException("Information Expired");
            }
        } else {
            throw new InformationExpiredException("Information Expired");
        }
    }

    private void generateMemberTransaction(OtcCoin coin, OtcOrder order, TransactionType type, long memberId, BigDecimal fee) {
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setType(type.getCode());
        memberTransaction.setFee(fee);
        memberTransaction.setMemberId(memberId);
        memberTransaction.setAmount(order.getNumber());
        memberTransaction.setDiscountFee("0");
        memberTransaction.setRealFee(fee + "");
        memberTransaction.setCreateTime(new Date());
        memberTransactionService.save(memberTransaction);
    }
}

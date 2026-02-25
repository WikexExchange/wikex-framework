package com.wikex.wikex.exchange.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.config.SnowflakeConfig;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.exchange.entity.OrderDetailAggregation;
import com.wikex.wikex.exchange.mapper.ExchangeOrderMapper;
import com.wikex.wikex.exchange.repository.ExchangeOrderDetailRepository;
import com.wikex.wikex.exchange.repository.OrderDetailAggregationRepository;
import com.wikex.wikex.exchange.service.ExchangeCoinService;
import com.wikex.wikex.exchange.service.ExchangeOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.exchange.util.OrderUtils;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.screen.ExchangeOrderScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ExchangeOrderServiceImpl extends ServiceImpl<ExchangeOrderMapper, ExchangeOrder>
        implements ExchangeOrderService {

    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private ExchangeOrderDetailRepository exchangeOrderDetailRepository;
    @Autowired
    private ExchangeCoinService exchangeCoinService;
    @Autowired
    private OrderDetailAggregationRepository orderDetailAggregationRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;
    @Autowired
    private RewardRecordService rewardRecordService;
    @Autowired
    private ExchangeOrderDetailServiceImpl exchangeOrderDetailService;
    @Value("${channel.enable:false}")
    private Boolean channelEnable;
    @Value("${channel.exchange-rate:0.00}")
    private BigDecimal channelExchangeRate;
    @Autowired
    private SnowflakeConfig snowflakeConfig;
    @Autowired
    private LocaleMessageSourceService msService;

    @Transactional
    public MessageResult addOrder(Long memberId, ExchangeOrder order) {
        order.setTime(Calendar.getInstance().getTimeInMillis());
        order.setStatus(ExchangeOrderStatus.TRADING);
        order.setTradedAmount(BigDecimal.ZERO);
        order.setOrderId(snowflakeConfig.getOrderId("E"));
        if (memberId != 1) {
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(order.getBaseSymbol(), memberId);
                if (wallet.getIsLock().equals(BooleanEnum.IS_TRUE)) {
                    return MessageResult.error(500, msService.getMessage("WALLET_LOCKED"));
                }
                BigDecimal turnover;
                if (order.getType() == ExchangeOrderType.MARKET_PRICE) {
                    turnover = order.getAmount();
                } else {
                    turnover = order.getAmount().multiply(order.getPrice());
                }
                if (wallet.getBalance().compareTo(turnover) < 0) {
                    return MessageResult.error(500, msService.getMessage("BALANCE_NOT_ENOUGH"));
                } else {
                    memberWalletService.freezeBalance(wallet.getId(), turnover);
                }
            } else if (order.getDirection() == ExchangeOrderDirection.SELL) {
                MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(order.getCoinSymbol(), memberId);
                if (wallet.getIsLock().equals(BooleanEnum.IS_TRUE)) {
                    return MessageResult.error(500, msService.getMessage("WALLET_LOCKED"));
                }
                if (wallet.getBalance().compareTo(order.getAmount()) < 0) {
                    return MessageResult.error(500, msService.getMessage("INSUFFICIENT_COIN") + order.getCoinSymbol());
                } else {
                    memberWalletService.freezeBalance(wallet.getId(), order.getAmount());
                }
            }
        }
        if (this.save(order)) {
            return MessageResult.success(msService.getMessage("EX_CORE_SUCCESS"));
        } else {
            return MessageResult.error(500, msService.getMessage("EX_CORE_ERROR"));
        }
    }

    public IPage<ExchangeOrder> findHistory(Long uid, String symbol, int pageNo, int pageSize) {
        IPage<ExchangeOrder> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ExchangeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(symbol), "symbol", symbol);
        queryWrapper.eq(uid != null, "member_id", uid);
        queryWrapper.ne("status", ExchangeOrderStatus.TRADING.getCode());
        queryWrapper.orderByDesc("time");
        return this.page(page, queryWrapper);
    }

    public List<ExchangeOrder> queryHistoryDelete(long beforeTime, int limit) {
        return this.baseMapper.queryHistoryDeleteList(beforeTime, limit);
    }

    public int deleteHistory(long beforeTime) {
        return this.baseMapper.deleteHistory(beforeTime);
    }

    public Page<ExchangeOrder> findPersonalHistory(Long uid, String symbol, ExchangeOrderType type,
            ExchangeOrderStatus status, String startTime, String endTime, ExchangeOrderDirection direction, int pageNo,
            int pageSize) {
        Page<ExchangeOrder> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ExchangeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(symbol), "symbol", symbol);
        if (type != null) {
            queryWrapper.eq("type", type.getCode());
        }
        if (direction != null) {
            queryWrapper.eq("direction", direction.getCode());
        }
        queryWrapper.eq(uid != null, "member_id", uid);

        if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
            queryWrapper.ge(startTime != null, "time", Long.valueOf(startTime));
            queryWrapper.le(endTime != null, "time", Long.valueOf(endTime));
        }
        if (status == null) {
            queryWrapper.ne("status", ExchangeOrderStatus.TRADING.getCode());
        } else {
            queryWrapper.eq("status", status.getCode());
        }
        queryWrapper.orderByDesc("time");
        return this.page(page, queryWrapper);

    }

    public Page<ExchangeOrder> findPersonalCurrent(Long uid, String symbol, ExchangeOrderType type, String startTime,
            String endTime, ExchangeOrderDirection direction, int pageNo, int pageSize) {
        Page<ExchangeOrder> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ExchangeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(symbol), "symbol", symbol);
        if (type != null) {
            queryWrapper.eq("type", type.getCode());
        }
        if (direction != null) {
            queryWrapper.eq("direction", direction.getCode());
        }
        queryWrapper.eq(uid != null, "member_id", uid);

        if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
            queryWrapper.ge("time", Long.valueOf(startTime));
            queryWrapper.le("time", Long.valueOf(endTime));
        }
        queryWrapper.eq("status", ExchangeOrderStatus.TRADING.getCode());
        queryWrapper.orderByDesc("time");
        return this.page(page, queryWrapper);

    }

    public Page<ExchangeOrder> findCurrent(Long uid, String symbol, int pageNo, int pageSize) {
        Page<ExchangeOrder> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ExchangeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(symbol), "symbol", symbol);
        queryWrapper.eq(uid != null, "member_id", uid);
        queryWrapper.eq("status", ExchangeOrderStatus.TRADING.getCode());
        queryWrapper.orderByDesc("time");
        return this.page(page, queryWrapper);
    }

    public Page<ExchangeOrder> findCurrentOverTime(Long uid, String symbol, int pageNo, int pageSize, Long time) {
        Page<ExchangeOrder> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ExchangeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(symbol), "symbol", symbol);
        queryWrapper.eq(uid != null, "member_id", uid);
        queryWrapper.eq("status", ExchangeOrderStatus.TRADING.getCode());
        queryWrapper.lt("time", time);
        return this.page(page, queryWrapper);
    }

    @Transactional
    public MessageResult processExchangeTrade(ExchangeTrade trade, boolean secondReferrerAward) throws Exception {

        if (trade == null || trade.getBuyOrderId() == null || trade.getSellOrderId() == null) {
            return MessageResult.error(500, "trade is null");
        }
        ExchangeOrder buyOrder = this.baseMapper.findByOrderId(trade.getBuyOrderId());
        ExchangeOrder sellOrder = this.baseMapper.findByOrderId(trade.getSellOrderId());
        if (buyOrder == null || sellOrder == null) {
            log.error("order not found");
            return MessageResult.error(500, "order not found");
        }

        ExchangeCoin coin = exchangeCoinService.findBySymbol(buyOrder.getSymbol());
        if (coin == null) {
            log.error("invalid trade symbol {}", buyOrder.getSymbol());
            return MessageResult.error(500, "invalid trade symbol {}" + buyOrder.getSymbol());
        }

        processOrder(buyOrder, trade, coin, secondReferrerAward);

        processOrder(sellOrder, trade, coin, secondReferrerAward);
        return MessageResult.success("process success");
    }

    public void processOrder(ExchangeOrder order, ExchangeTrade trade, ExchangeCoin coin, boolean secondReferrerAward) {
        try {
            Long time = Calendar.getInstance().getTimeInMillis();

            ExchangeOrderDetail orderDetail = new ExchangeOrderDetail();
            orderDetail.setOrderId(order.getOrderId());
            orderDetail.setTime(time);
            orderDetail.setPrice(trade.getPrice());
            orderDetail.setAmount(trade.getAmount());

            BigDecimal incomeCoinAmount, turnover, outcomeCoinAmount;
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                turnover = trade.getBuyTurnover();
            } else {
                turnover = trade.getSellTurnover();
            }
            orderDetail.setTurnover(turnover);

            BigDecimal fee;
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                fee = trade.getAmount().multiply(coin.getFee());
            } else {
                fee = turnover.multiply(coin.getFee());
            }

            if (order.getMemberId() == 1 || order.getMemberId() == 10001) {
                fee = BigDecimal.ZERO;
            }
            orderDetail.setFee(fee);
            exchangeOrderDetailRepository.save(orderDetail);

            OrderDetailAggregation aggregation = new OrderDetailAggregation();
            aggregation.setType(OrderTypeEnum.EXCHANGE);
            aggregation.setAmount(order.getAmount().doubleValue());
            aggregation.setFee(orderDetail.getFee().doubleValue());
            aggregation.setTime(orderDetail.getTime());
            aggregation.setDirection(order.getDirection());
            aggregation.setOrderId(order.getOrderId());
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                aggregation.setUnit(order.getBaseSymbol());
            } else {
                aggregation.setUnit(order.getCoinSymbol());
            }
            Member member = memberService.getById(order.getMemberId());
            if (member != null) {
                aggregation.setMemberId(member.getId());
                aggregation.setUsername(member.getUsername());
                aggregation.setRealName(member.getRealName());
            }
            orderDetailAggregationRepository.save(aggregation);

            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                incomeCoinAmount = trade.getAmount().subtract(fee);
            } else {
                incomeCoinAmount = turnover.subtract(fee);
            }
            String incomeSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getCoinSymbol()
                    : order.getBaseSymbol();
            if (order.getMemberId() != 1) {
                MemberWallet incomeWallet = memberWalletService.findByCoinUnitAndMemberId(incomeSymbol,
                        order.getMemberId());
                memberWalletService.increaseBalance(incomeWallet.getId(), incomeCoinAmount);

                MemberTransaction transaction = new MemberTransaction();
                transaction.setAmount(incomeCoinAmount);
                transaction.setSymbol(incomeSymbol);
                transaction.setAddress("");
                transaction.setMemberId(incomeWallet.getMemberId());
                transaction.setType(TransactionType.EXCHANGE.getCode());
                transaction.setCreateTime(new Date());
                transaction.setFee(fee);
                transaction.setDiscountFee("0");
                transaction.setRealFee(fee.toString());
                memberTransactionService.save(transaction);
            }

            String outcomeSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getBaseSymbol()
                    : order.getCoinSymbol();
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                outcomeCoinAmount = turnover;
            } else {
                outcomeCoinAmount = trade.getAmount();
            }
            if (order.getMemberId() != 1) {
                MemberWallet outcomeWallet = memberWalletService.findByCoinUnitAndMemberId(outcomeSymbol,
                        order.getMemberId());
                memberWalletService.decreaseFrozen(outcomeWallet.getId(), outcomeCoinAmount);
                MemberTransaction transaction2 = new MemberTransaction();
                transaction2.setAmount(outcomeCoinAmount.negate());
                transaction2.setSymbol(outcomeSymbol);
                transaction2.setAddress("");
                transaction2.setMemberId(outcomeWallet.getMemberId());
                transaction2.setType(TransactionType.EXCHANGE.getCode());
                transaction2.setFee(BigDecimal.ZERO);
                transaction2.setRealFee("0");
                transaction2.setDiscountFee("0");
                transaction2.setCreateTime(new Date());
                memberTransactionService.save(transaction2);
            }
            try {

                if (order.getDirection() == ExchangeOrderDirection.SELL) {
                    // promoteReward(fee, member, incomeSymbol, secondReferrerAward);
                }
            } catch (Exception e) {
                // e.printStackTrace();
                log.error("Error 0 issuing spot trading promotional fee commission", e);

            }
        } catch (Exception e) {
            log.error("Error 1 issuing spot trading promotional fee commission", e);
            // e.printStackTrace();
        }
    }

    public List<ExchangeOrderDetail> getAggregation(String orderId) {
        return exchangeOrderDetailService.findAllByOrderId(orderId);
    }

    public void promoteReward(BigDecimal fee, Member member, String incomeSymbol, boolean secondReferrerAward) {
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService
                .findByType(PromotionRewardType.EXCHANGE_TRANSACTION);
        if (rewardPromotionSetting != null && member.getInviterId() != null) {
            if (!(DateUtil.diffDays(new Date(), member.getRegistrationTime()) > rewardPromotionSetting
                    .getEffectiveTime())) {
                Member member1 = memberService.getById(member.getInviterId());
                MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(incomeSymbol,
                        member.getInviterId());
                JSONObject jsonObject = JSONObject.parseObject(rewardPromotionSetting.getInfo());
                BigDecimal reward = BigDecimalUtils.mulRound(fee,
                        BigDecimalUtils.getRate(jsonObject.getBigDecimal("one")), 8);
                if (reward.compareTo(BigDecimal.ZERO) > 0) {
                    memberWalletService.increaseBalance(memberWallet.getId(), reward);
                    MemberTransaction memberTransaction = new MemberTransaction();
                    memberTransaction.setAmount(reward);
                    memberTransaction.setFee(BigDecimal.ZERO);
                    memberTransaction.setMemberId(member1.getId());
                    memberTransaction.setSymbol(incomeSymbol);
                    memberTransaction.setType(TransactionType.PROMOTION_AWARD.getCode());
                    memberTransaction.setDiscountFee("0");
                    memberTransaction.setRealFee("0");
                    memberTransaction.setCreateTime(new Date());
                    memberTransactionService.save(memberTransaction);
                    RewardRecord rewardRecord1 = new RewardRecord();
                    rewardRecord1.setAmount(reward);
                    rewardRecord1.setCoinId(memberWallet.getCoinId());
                    rewardRecord1.setMemberId(member1.getId());
                    rewardRecord1.setRemark(rewardPromotionSetting.getType().getDescription());
                    rewardRecord1.setType(RewardRecordType.PROMOTION);
                    rewardRecordService.save(rewardRecord1);
                }

                if (secondReferrerAward == false) {

                    return;
                }
                if (member1.getInviterId() != null && !(DateUtil.diffDays(new Date(),
                        member1.getRegistrationTime()) > rewardPromotionSetting.getEffectiveTime())) {
                    Member member2 = memberService.getById(member1.getInviterId());
                    MemberWallet memberWallet1 = memberWalletService.findByCoinUnitAndMemberId(incomeSymbol,
                            member2.getId());

                    BigDecimal reward1 = BigDecimalUtils.mulRound(fee,
                            BigDecimalUtils.getRate(jsonObject.getBigDecimal("two")), 8);
                    if (reward1.compareTo(BigDecimal.ZERO) > 0) {

                        memberWalletService.increaseBalance(memberWallet1.getId(), reward);
                        MemberTransaction memberTransaction = new MemberTransaction();
                        memberTransaction.setAmount(reward1);
                        memberTransaction.setFee(BigDecimal.ZERO);
                        memberTransaction.setMemberId(member2.getId());
                        memberTransaction.setSymbol(incomeSymbol);
                        memberTransaction.setType(TransactionType.PROMOTION_AWARD.getCode());
                        memberTransaction.setCreateTime(new Date());
                        memberTransactionService.save(memberTransaction);

                        RewardRecord rewardRecord1 = new RewardRecord();
                        rewardRecord1.setAmount(reward1);
                        rewardRecord1.setCoinId(memberWallet1.getCoinId());
                        rewardRecord1.setMemberId(member2.getId());
                        rewardRecord1.setRemark(rewardPromotionSetting.getType().getDescription());
                        rewardRecord1.setType(RewardRecordType.PROMOTION);
                        rewardRecordService.save(rewardRecord1);
                    }
                }
            }
        }
    }

    public List<ExchangeOrder> findAllTradingOrderBySymbol(String symbol) {
        QueryWrapper<ExchangeOrder> query = new QueryWrapper<>();
        query.eq("symbol", symbol);
        query.eq("status", ExchangeOrderStatus.TRADING.getCode());
        return this.list(query);
    }

    @Override
    public List<ExchangeOrder> findByOrderIds(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<ExchangeOrder> query = new QueryWrapper<>();
        query.in("order_id", orderIds);
        return this.list(query);
    }

    @Transactional
    public MessageResult tradeCompleted(String orderId, BigDecimal tradedAmount, BigDecimal turnover) {
        ExchangeOrder order = this.baseMapper.findByOrderId(orderId);
        if (order == null) {
            log.error("order:(" + orderId + "), does not exist");
            return MessageResult.error(500, "order:(" + orderId + "), does not exist");
        }
        if (order.getStatus() != null && order.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, "invalid order(" + orderId + "),not trading status");
        }
        order.setTradedAmount(tradedAmount);
        order.setTurnover(turnover);
        order.setStatus(ExchangeOrderStatus.COMPLETED);
        order.setCompletedTime(Calendar.getInstance().getTimeInMillis());
        this.updateById(order);

        orderRefund(order, tradedAmount, turnover);

        return MessageResult.success("tradeCompleted success");
    }

    public void orderRefund(ExchangeOrder order, BigDecimal tradedAmount, BigDecimal turnover) {

        BigDecimal frozenBalance, dealBalance;
        if (order.getDirection() == ExchangeOrderDirection.BUY) {
            if (order.getType() == ExchangeOrderType.LIMIT_PRICE) {
                frozenBalance = order.getAmount().multiply(order.getPrice());
            } else {
                frozenBalance = order.getAmount();
            }
            dealBalance = turnover;
        } else {
            frozenBalance = order.getAmount();
            dealBalance = tradedAmount;
        }
        String coinSymbol = order.getDirection() == ExchangeOrderDirection.BUY ? order.getBaseSymbol()
                : order.getCoinSymbol();

        BigDecimal refundAmount = frozenBalance.subtract(dealBalance);
        // System.out.println("Refund: " + refundAmount);

        if (refundAmount.compareTo(BigDecimal.ZERO) > 0 && order.getMemberId() != 1) {
            MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(coinSymbol, order.getMemberId());
            if (memberWallet == null) {
                log.error(
                        "===cancel==Refund: wallet does not exist::" + coinSymbol + " memberId:" + order.getMemberId());
                return;
            }
            memberWalletService.thawBalance(memberWallet.getId(), refundAmount);
        }
    }

    @Transactional
    public MessageResult cancelOrder(String orderId, BigDecimal tradedAmount, BigDecimal turnover) {
        ExchangeOrder order = this.getById(orderId);
        if (order == null) {
            return MessageResult.error("order not exists");
        }
        if (order.getStatus() != ExchangeOrderStatus.TRADING) {
            return MessageResult.error(500, "order not in trading");
        }
        order.setTradedAmount(tradedAmount);
        order.setTurnover(turnover);
        order.setStatus(ExchangeOrderStatus.CANCELED);
        order.setCanceledTime(Calendar.getInstance().getTimeInMillis());
        this.updateById(order);

        orderRefund(order, tradedAmount, turnover);
        return MessageResult.success();
    }

    public long findTodayOrderCancelTimes(Long uid, String symbol) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTick = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        long endTick = calendar.getTimeInMillis();

        QueryWrapper<ExchangeOrder> query = new QueryWrapper<>();
        query.eq("symbol", symbol);
        query.eq("member_id", uid);
        query.eq("status", ExchangeOrderStatus.CANCELED.getCode());
        query.ge("canceledTime", startTick);
        query.lt("canceledTime", endTick);
        return this.count(query);

    }

    public long findCurrentTradingCount(Long uid, String symbol) {

        QueryWrapper<ExchangeOrder> query = new QueryWrapper<>();
        query.eq("symbol", symbol);
        query.eq("member_id", uid);
        query.eq("status", ExchangeOrderStatus.TRADING.getCode());
        return this.count(query);
    }

    public long findCurrentTradingCount(Long uid, String symbol, ExchangeOrderDirection direction) {

        QueryWrapper<ExchangeOrder> query = new QueryWrapper<>();
        query.eq("symbol", symbol);
        query.eq("member_id", uid);
        query.eq("direction", direction.getCode());
        query.eq("status", ExchangeOrderStatus.TRADING.getCode());
        return this.count(query);

    }

    public List<ExchangeOrder> findOvertimeOrder(String symbol, int maxTradingTime) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -maxTradingTime);
        long tickTime = calendar.getTimeInMillis();
        QueryWrapper<ExchangeOrder> query = new QueryWrapper<>();
        query.eq("symbol", symbol);
        query.eq("status", ExchangeOrderStatus.TRADING.getCode());
        query.lt("time", tickTime);
        return this.list(query);
    }

    public List<ExchangeOrder> queryExchangeOrderByTime(long cancelTime) {
        return this.baseMapper.queryExchangeOrderByTime(cancelTime);
    }

    public List<ExchangeOrder> queryExchangeOrderByTimeById(long cancelTime, long sellMemberId, long buyMemberId) {
        return this.baseMapper.queryExchangeOrderByTimeById(cancelTime, sellMemberId, buyMemberId);
    }

    public List<ExchangeOrder> queryExchangeOrderByTimeById(long cancelTime) {
        return this.baseMapper.queryExchangeOrderByCancelTime(cancelTime);
    }

    @Transactional
    public String addOrderForApi(Long memberId, ExchangeOrder order) {
        order.setTime(Calendar.getInstance().getTimeInMillis());
        order.setStatus(ExchangeOrderStatus.TRADING);
        order.setTradedAmount(BigDecimal.ZERO);
        order.setOrderId(snowflakeConfig.getOrderId("E"));
        if (memberId != 1) {
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(order.getBaseSymbol(), memberId);
                BigDecimal turnover;
                if (order.getType() == ExchangeOrderType.MARKET_PRICE) {
                    turnover = order.getAmount();
                } else {
                    turnover = order.getAmount().multiply(order.getPrice());
                }
                if (wallet.getBalance().compareTo(turnover) < 0) {
                    return null;
                } else {
                    memberWalletService.freezeBalance(wallet.getId(), turnover);
                    // wallet.setBalance(wallet.getBalance().subtract(turnover));
                    // wallet.setFrozenBalance(wallet.getFrozenBalance().add(turnover));
                }
            } else if (order.getDirection() == ExchangeOrderDirection.SELL) {

                MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(order.getCoinSymbol(), memberId);
                if (wallet.getBalance().compareTo(order.getAmount()) < 0) {
                    return null;
                } else {
                    memberWalletService.freezeBalance(wallet.getId(), order.getAmount());
                    // wallet.setBalance(wallet.getBalance().subtract(order.getAmount()));
                    // wallet.setFrozenBalance(wallet.getFrozenBalance().add(order.getAmount()));
                }
            }
        }
        this.saveOrUpdate(order);
        return order.getOrderId();
    }

    public Page<ExchangeOrder> findCurrentTradingOrderForApi(long memberId, String symbol,
            ExchangeOrderDirection direction, int pageNo, int pageSize) {

        Page<ExchangeOrder> page = new Page<>(pageNo, pageSize);
        QueryWrapper<ExchangeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(symbol), "symbol", symbol);
        queryWrapper.eq("member_id", memberId);
        queryWrapper.eq("status", ExchangeOrderStatus.TRADING.getCode());
        queryWrapper.eq("direction", direction.getCode());
        queryWrapper.orderByDesc("time");
        return this.page(page, queryWrapper);

    }

    @Transactional
    public void forceCancelOrder(ExchangeOrder order) {
        List<ExchangeOrderDetail> details = exchangeOrderDetailService.findAllByOrderId(order.getOrderId());
        BigDecimal tradedAmount = BigDecimal.ZERO;
        BigDecimal turnover = BigDecimal.ZERO;
        for (ExchangeOrderDetail trade : details) {
            tradedAmount = tradedAmount.add(trade.getAmount());
            turnover = turnover.add(trade.getAmount().multiply(trade.getPrice()));
        }
        order.setTradedAmount(tradedAmount);
        order.setTurnover(turnover);
        if (OrderUtils.isCompleted(order)) {
            tradeCompleted(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
        } else {
            cancelOrder(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        this.removeById(id);
    }

    @Override
    public Page<ExchangeOrder> findAll(ExchangeOrderScreen screen) {
        Page<ExchangeOrder> page = new Page<>(screen.getPageNo(), screen.getPageSize());
        LambdaQueryWrapper<ExchangeOrder> queryWrapper = new LambdaQueryWrapper<>();

        if (screen.getOrderDirection() != null) {
            queryWrapper.eq(ExchangeOrder::getDirection, screen.getOrderDirection().getCode());
        }
        if (StringUtils.isNotEmpty(screen.getOrderId())) {
            queryWrapper.eq(ExchangeOrder::getOrderId, screen.getOrderId());
        }
        if (screen.getMemberId() != null) {
            queryWrapper.eq(ExchangeOrder::getMemberId, screen.getMemberId());
        }
        if (screen.getType() != null) {
            queryWrapper.eq(ExchangeOrder::getType, screen.getType().getCode());
        }
        if (StringUtils.isNotBlank(screen.getCoinSymbol())) {
            queryWrapper.eq(ExchangeOrder::getCoinSymbol, screen.getCoinSymbol());
        }
        if (StringUtils.isNotBlank(screen.getBaseSymbol())) {
            queryWrapper.eq(ExchangeOrder::getBaseSymbol, screen.getBaseSymbol());
        }
        if (screen.getStatus() != null) {
            queryWrapper.eq(ExchangeOrder::getStatus, screen.getStatus().getCode());
        }
        if (screen.getMinPrice() != null) {
            queryWrapper.ge(ExchangeOrder::getPrice, screen.getMinPrice());
        }
        if (screen.getMaxPrice() != null) {
            queryWrapper.le(ExchangeOrder::getPrice, screen.getMaxPrice());
        }
        if (screen.getMinTradeAmount() != null) {
            queryWrapper.ge(ExchangeOrder::getTradedAmount, screen.getMinTradeAmount());
        }
        if (screen.getMaxTradeAmount() != null) {
            queryWrapper.le(ExchangeOrder::getTradedAmount, screen.getMaxTradeAmount());
        }
        if (screen.getMinTurnOver() != null) {
            queryWrapper.ge(ExchangeOrder::getTurnover, screen.getMinTurnOver());
        }
        if (screen.getMaxTurnOver() != null) {
            queryWrapper.le(ExchangeOrder::getTurnover, screen.getMaxTurnOver());
        }
        if (screen.getRobotOrder() != null && screen.getRobotOrder() == 1) {

            queryWrapper.notIn(ExchangeOrder::getMemberId, 1, 2, 10001);

        }
        if (screen.getRobotOrder() != null && screen.getRobotOrder() == 0) {

            queryWrapper.in(ExchangeOrder::getMemberId, 1, 2, 10001);

        }
        if (screen.getCompleted() != null) {
            if (screen.getCompleted() == BooleanEnum.IS_FALSE) {
                queryWrapper.isNull(ExchangeOrder::getCompletedTime);
                queryWrapper.isNull(ExchangeOrder::getCanceledTime);
                queryWrapper.eq(ExchangeOrder::getStatus, ExchangeOrderStatus.TRADING.getCode());
            } else {

                queryWrapper.and(wrapper -> wrapper.isNotNull(ExchangeOrder::getCompletedTime)
                        .or().isNotNull(ExchangeOrder::getCanceledTime)
                        .or().ne(ExchangeOrder::getStatus, ExchangeOrderStatus.TRADING.getCode()));
            }
        }
        queryWrapper.orderByDesc(ExchangeOrder::getTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public List<ExchangeOrder> getExchangeTurnoverBase(String dateStr) {
        long[] range = resolveCompletedTimeRange(dateStr);
        return this.baseMapper.getExchangeTurnoverBase(range[0], range[1]);
    }

    @Override
    public List<ExchangeOrder> getExchangeTurnoverCoin(String dateStr) {
        long[] range = resolveCompletedTimeRange(dateStr);
        return this.baseMapper.getExchangeTurnoverCoin(range[0], range[1]);
    }

    @Override
    public List<ExchangeOrder> getExchangeTurnoverSymbol(String dateStr) {
        long[] range = resolveCompletedTimeRange(dateStr);
        return this.baseMapper.getExchangeTurnoverSymbol(range[0], range[1]);
    }

    private long[] resolveCompletedTimeRange(String dateStr) {
        try {
            Date startDate = DateUtil.strToYYMMDDDate(dateStr);
            if (startDate == null) {
                return new long[]{0L, 0L};
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return new long[]{startDate.getTime(), calendar.getTimeInMillis()};
        } catch (Exception e){
            return new long[]{0L, 0L};
        }
    }

}

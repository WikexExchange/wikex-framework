package com.wikex.wikex.exchange.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.constant.ExchangeOrderStatus;
import com.wikex.wikex.constant.ExchangeOrderType;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.screen.ExchangeOrderScreen;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.util.MessageResult;

import java.math.BigDecimal;
import java.util.List;


public interface ExchangeOrderService extends IService<ExchangeOrder> {
    
    public MessageResult addOrder(Long memberId, ExchangeOrder order);

    public IPage<ExchangeOrder> findHistory(Long uid, String symbol, int pageNo, int pageSize) ;

    public List<ExchangeOrder> queryHistoryDelete(long beforeTime, int limit);
    
    public int deleteHistory(long beforeTime) ;

    public Page<ExchangeOrder> findPersonalHistory(Long uid, String symbol, ExchangeOrderType type, ExchangeOrderStatus status, String startTime, String endTime, ExchangeOrderDirection direction, int pageNo, int pageSize);

    public Page<ExchangeOrder> findPersonalCurrent(Long uid, String symbol, ExchangeOrderType type, String startTime, String endTime, ExchangeOrderDirection direction, int pageNo, int pageSize);
    
    public Page<ExchangeOrder> findCurrent(Long uid, String symbol, int pageNo, int pageSize) ;

    public Page<ExchangeOrder> findCurrentOverTime(Long uid, String symbol, int pageNo, int pageSize, Long time);
    
    public MessageResult processExchangeTrade(ExchangeTrade trade, boolean secondReferrerAward) throws Exception ;

    public void processOrder(ExchangeOrder order, ExchangeTrade trade, ExchangeCoin coin,boolean secondReferrerAward) ;

    public List<ExchangeOrderDetail> getAggregation(String orderId) ;

    public void promoteReward(BigDecimal fee, Member member, String incomeSymbol, boolean secondReferrerAward) ;

    public List<ExchangeOrder> findAllTradingOrderBySymbol(String symbol) ;

    public List<ExchangeOrder> findByOrderIds(List<String> orderIds);

    public MessageResult tradeCompleted(String orderId, BigDecimal tradedAmount, BigDecimal turnover) ;

    public void orderRefund(ExchangeOrder order, BigDecimal tradedAmount, BigDecimal turnover) ;

    public MessageResult cancelOrder(String orderId, BigDecimal tradedAmount, BigDecimal turnover) ;

    public long findTodayOrderCancelTimes(Long uid, String symbol) ;

    public long findCurrentTradingCount(Long uid, String symbol) ;

    public long findCurrentTradingCount(Long uid, String symbol, ExchangeOrderDirection direction) ;

    public List<ExchangeOrder> findOvertimeOrder(String symbol, int maxTradingTime) ;

    public List<ExchangeOrder> queryExchangeOrderByTime(long cancelTime) ;

    public List<ExchangeOrder> queryExchangeOrderByTimeById(long cancelTime,long sellMemberId,long buyMemberId) ;

    public List<ExchangeOrder> queryExchangeOrderByTimeById(long cancelTime) ;
    
    public String addOrderForApi(Long memberId, ExchangeOrder order) ;

    public Page<ExchangeOrder> findCurrentTradingOrderForApi(long memberId, String symbol, ExchangeOrderDirection direction, int pageNo, int pageSize) ;

    public void forceCancelOrder(ExchangeOrder order);

    public void delete(String id) ;

    public Page<ExchangeOrder> findAll(ExchangeOrderScreen screen);

    public List<ExchangeOrder> getExchangeTurnoverBase(String dateStr);

    public List<ExchangeOrder> getExchangeTurnoverCoin(String dateStr);

    public List<ExchangeOrder> getExchangeTurnoverSymbol(String dateStr);
}

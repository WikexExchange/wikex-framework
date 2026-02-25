package com.wikex.wikex.match.service;

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

/**
 * <p>
 * Spot trading order service interface
 * </p>
 *
 * @author markchao
 * @since 2022-02-07
 */
public interface ExchangeOrderService extends IService<ExchangeOrder> {

    /**
     * Add a new order
     *
     * @param memberId Member ID
     * @param order    Order entity
     * @return MessageResult
     */
    public MessageResult addOrder(Long memberId, ExchangeOrder order);

    /**
     * Find historical orders
     *
     * @param uid      User ID
     * @param symbol   Trading pair
     * @param pageNo   Page number
     * @param pageSize Page size
     * @return Page of ExchangeOrder
     */
    public IPage<ExchangeOrder> findHistory(Long uid, String symbol, int pageNo, int pageSize);

    /**
     * Query deletable orders before a given time
     * 
     * @param beforeTime timestamp
     * @param limit      max number of records
     * @return list of orders
     */
    public List<ExchangeOrder> queryHistoryDelete(long beforeTime, int limit);

    /**
     * Delete historical orders before a given time
     * 
     * @param beforeTime timestamp
     * @return number of deleted records
     */
    public int deleteHistory(long beforeTime);

    /**
     * Personal center historical orders
     * 
     * @param uid       User ID
     * @param symbol    Trading pair
     * @param type      Order type
     * @param status    Order status
     * @param startTime Start time
     * @param endTime   End time
     * @param direction Order direction
     * @param pageNo    Page number
     * @param pageSize  Page size
     * @return Page of ExchangeOrder
     */
    public Page<ExchangeOrder> findPersonalHistory(Long uid, String symbol, ExchangeOrderType type,
            ExchangeOrderStatus status, String startTime, String endTime, ExchangeOrderDirection direction, int pageNo,
            int pageSize);

    /**
     * Personal center current orders
     *
     * @param uid       User ID
     * @param symbol    Trading pair
     * @param type      Order type
     * @param startTime Start time
     * @param endTime   End time
     * @param direction Order direction
     * @param pageNo    Page number
     * @param pageSize  Page size
     * @return Page of ExchangeOrder
     */
    public Page<ExchangeOrder> findPersonalCurrent(Long uid, String symbol, ExchangeOrderType type, String startTime,
            String endTime, ExchangeOrderDirection direction, int pageNo, int pageSize);

    /**
     * Query current orders in trading
     *
     * @param uid      User ID
     * @param symbol   Trading pair
     * @param pageNo   Page number
     * @param pageSize Page size
     * @return Page of ExchangeOrder
     */
    public Page<ExchangeOrder> findCurrent(Long uid, String symbol, int pageNo, int pageSize);

    /**
     * Process trade matching
     *
     * @param trade               Trade object
     * @param secondReferrerAward whether to give commission to second-level
     *                            referrer (true = give commission)
     * @return MessageResult
     * @throws Exception
     */
    public MessageResult processExchangeTrade(ExchangeTrade trade, boolean secondReferrerAward) throws Exception;

    /**
     * Process wallet updates for matched orders
     *
     * @param order               Order object
     * @param trade               Trade detail
     * @param coin                Trading coin information (fees, etc.)
     * @param secondReferrerAward whether to give commission to second-level
     *                            referrer
     */
    public void processOrder(ExchangeOrder order, ExchangeTrade trade, ExchangeCoin coin, boolean secondReferrerAward);

    /**
     * Get aggregated order details
     * 
     * @param orderId Order ID
     * @return List of ExchangeOrderDetail
     */
    public List<ExchangeOrderDetail> getAggregation(String orderId);

    /**
     * Trading fee commission rebate
     *
     * @param fee                 Fee amount
     * @param member              Order owner
     * @param incomeSymbol        Coin symbol
     * @param secondReferrerAward whether to give commission to second-level
     *                            referrer
     */
    public void promoteReward(BigDecimal fee, Member member, String incomeSymbol, boolean secondReferrerAward);

    /**
     * Find all uncompleted orders by symbol
     *
     * @param symbol Trading pair
     * @return List of ExchangeOrder
     */
    public List<ExchangeOrder> findAllTradingOrderBySymbol(String symbol);

    /**
     * Mark order as completed
     *
     * @param orderId      Order ID
     * @param tradedAmount Traded amount
     * @param turnover     Turnover
     * @return MessageResult
     */
    public MessageResult tradeCompleted(String orderId, BigDecimal tradedAmount, BigDecimal turnover);

    /**
     * Refund remaining funds when order is cancelled or partially filled
     *
     * @param order        Order object
     * @param tradedAmount Traded amount
     * @param turnover     Turnover
     */
    public void orderRefund(ExchangeOrder order, BigDecimal tradedAmount, BigDecimal turnover);

    /**
     * Cancel order
     *
     * @param orderId      Order ID
     * @param tradedAmount Traded amount
     * @param turnover     Turnover
     * @return MessageResult
     */
    public MessageResult cancelOrder(String orderId, BigDecimal tradedAmount, BigDecimal turnover);

    /**
     * Get the number of cancellations for a trading pair today
     *
     * @param uid    User ID
     * @param symbol Trading pair
     * @return number of cancellations
     */
    public long findTodayOrderCancelTimes(Long uid, String symbol);

    /**
     * Get the number of currently active orders
     *
     * @param uid    User ID
     * @param symbol Trading pair
     * @return count
     */
    public long findCurrentTradingCount(Long uid, String symbol);

    public long findCurrentTradingCount(Long uid, String symbol, ExchangeOrderDirection direction);

    /**
     * Find overtime orders
     * 
     * @param symbol         Trading pair
     * @param maxTradingTime Maximum allowed trading time
     * @return list of overtime orders
     */
    public List<ExchangeOrder> findOvertimeOrder(String symbol, int maxTradingTime);

    /**
     * Query orders matching a certain status by time
     *
     * @param cancelTime timestamp
     * @return List of ExchangeOrder
     */
    public List<ExchangeOrder> queryExchangeOrderByTime(long cancelTime);

    public List<ExchangeOrder> queryExchangeOrderByTimeById(long cancelTime, long sellMemberId, long buyMemberId);

    /**
     * Query orders matching a certain status by time
     *
     * @param cancelTime timestamp
     * @return List of ExchangeOrder
     */
    public List<ExchangeOrder> queryExchangeOrderByTimeById(long cancelTime);

    /**
     * API - Add order
     *
     * @param memberId Member ID
     * @param order    Order entity
     * @return Order ID as String
     */
    public String addOrderForApi(Long memberId, ExchangeOrder order);

    /**
     * API - Query current trading orders
     *
     * @param memberId  Member ID
     * @param symbol    Trading pair
     * @param direction Order direction
     * @param pageNo    Page number
     * @param pageSize  Page size
     * @return Page of ExchangeOrder
     */
    public Page<ExchangeOrder> findCurrentTradingOrderForApi(long memberId, String symbol,
            ExchangeOrderDirection direction, int pageNo, int pageSize);

    /**
     * Force cancel order - used when there is inconsistency between matching engine
     * and database
     * 
     * @param order ExchangeOrder
     */
    public void forceCancelOrder(ExchangeOrder order);

    public void delete(String id);

    public Page<ExchangeOrder> findAll(ExchangeOrderScreen screen);

    public List<ExchangeOrder> getExchangeTurnoverBase(String dateStr);

    public List<ExchangeOrder> getExchangeTurnoverCoin(String dateStr);

    public List<ExchangeOrder> getExchangeTurnoverSymbol(String dateStr);
}

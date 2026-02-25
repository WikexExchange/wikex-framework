package com.wikex.wikex.swap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.TradingTimes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 * Perpetual Contract Trading Pair Trading Times
 * </p>
 * 
 * @author sulinxin
 * @since 2021-08-23
 */
@FeignClient(value = "wikex-swap",contextId = "tradingTimesFeign")
public interface TradingTimesFeign {

    /**
     * Find trading times by contract coin ID
     * 
     * @param contractId the contract coin ID
     * @return list of TradingTimes
     */
    @PostMapping("tradingTimesFeign/findByCoinId")
    public List<TradingTimes> findByCoinId(@RequestParam("contractId") Long contractId);

    /**
     * Save a TradingTimes entry
     * 
     * @param tradingTimes TradingTimes entity to save
     * @return saved TradingTimes
     */
    @PostMapping("tradingTimesFeign/save")
    public TradingTimes save(@RequestBody TradingTimes tradingTimes) ;

    /**
     * Paginated query of TradingTimes entries
     * 
     * @param pageParam pagination parameters
     * @return paginated TradingTimes page
     */
    @PostMapping("tradingTimesFeign/findAll")
    Page<TradingTimes> findAll(@RequestBody PageParam pageParam);
}

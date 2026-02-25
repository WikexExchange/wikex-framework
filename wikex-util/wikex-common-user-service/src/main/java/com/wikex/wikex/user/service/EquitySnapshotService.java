package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.EquitySnapshot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface EquitySnapshotService extends IService<EquitySnapshot> {

    Map<String, Object> calculateTodaySnapshot(Long memberId);

    void saveTodaySnapshotBatch(List<Long> memberIds, Map<String, BigDecimal> priceMap);

    List<Map<String, Object>> getEquityTrend(Long memberId, int days);

    Map<String, Object> getSummary(Long memberId);

    Map<String, Object> getCumulativePnl(Long memberId, int days);

    List<Map<String, Object>> getDailyChart(Long memberId);

    List<Map<String, Object>> getDailyCalendarMonth(Long memberId, int year, int month);

    List<Map<String, Object>> getDailyCalendarYear(Long memberId, int year);

    List<Map<String, Object>> getSpotEquityTrend(Long memberId, int days);

    Map<String, Object> getSpotAssetBreakdown(Long memberId, int limitTop);
}
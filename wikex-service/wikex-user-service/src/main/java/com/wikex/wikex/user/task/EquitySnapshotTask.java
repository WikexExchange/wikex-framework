package com.wikex.wikex.user.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.service.EquitySnapshotService;
import com.wikex.wikex.user.service.MemberService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Equity Snapshot Scheduler Task
 * Capture daily equity snapshot for all members
 */
@Component
@Slf4j
public class EquitySnapshotTask {

    @Autowired
    private EquitySnapshotService equitySnapshotService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MarketFeign marketFeign;

    private static final int PAGE_SIZE = 500;

    /**
     * Run daily at 23:50 UTC
     */
    @XxlJob("dailyEquitySnapshotTask")
    public void dailyEquitySnapshotTask() {
        log.info("===>>> Start daily equity snapshot task");

        Map<String, BigDecimal> priceMap = loadPriceMap();

        long lastMemberId = 0L;
        long totalMembers = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            while (true) {
                List<Member> members = memberService.list(
                        new LambdaQueryWrapper<Member>()
                                .gt(Member::getId, lastMemberId)
                                .ne(Member::getId, 1)
                                .orderByAsc(Member::getId)
                                .last("LIMIT " + PAGE_SIZE));

                if (members.isEmpty()) {
                    break;
                }

                lastMemberId = members.get(members.size() - 1).getId();
                totalMembers += members.size();

                List<Long> memberIds = members.stream().map(Member::getId).collect(Collectors.toList());

                try {
                    equitySnapshotService.saveTodaySnapshotBatch(memberIds, priceMap);
                    successCount += memberIds.size();
                } catch (Exception e) {
                    failCount += memberIds.size();
                    log.error("Batch failed, size={}, lastMemberId={}", memberIds.size(), lastMemberId, e);
                }
                log.info("Batch processed: size={}, success={}, failed={}", members.size(), successCount, failCount);
            }
            log.info("Daily equity snapshot finished: totalMembers={}, success={}, failed={}", totalMembers,
                    successCount, failCount);
        } catch (Exception e) {
            log.error("===>>> Daily equity snapshot task crashed", e);
        }
    }

    /**
     * Manual trigger for testing or admin operations
     * Snapshots a specific member's equity
     */
    @XxlJob("manualEquitySnapshot")
    public void manualEquitySnapshot() {
        log.info("===>>> Manual equity snapshot triggered");

        // This can be extended to accept member ID parameter if needed
        // For now, it runs the same as daily task but can be triggered manually
        dailyEquitySnapshotTask();
    }

    private Map<String, BigDecimal> loadPriceMap() {
        List<CoinThumb> thumbs = marketFeign.findSymbolThumb4Feign();
        Map<String, BigDecimal> priceMap = new HashMap<>();
        priceMap.put("USDT", BigDecimal.ONE);

        if (thumbs == null) {
            return priceMap;
        }
        for (CoinThumb thumb : thumbs) {
            if (thumb.getSymbol() == null || thumb.getClose() == null) {
                continue;
            }
            String symbol = thumb.getSymbol().toUpperCase();
            BigDecimal close = thumb.getClose();
            if (symbol.endsWith("/USDT")) {
                priceMap.put(symbol.split("/")[0], close);
            } else if (symbol.startsWith("USDT/") && close.signum() > 0) {
                priceMap.put(symbol.split("/")[1], BigDecimal.ONE.divide(close, 8, RoundingMode.HALF_UP));
            }
        }
        return priceMap;
    }
}
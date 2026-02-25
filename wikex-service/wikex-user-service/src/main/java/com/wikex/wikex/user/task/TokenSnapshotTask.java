package com.wikex.wikex.user.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.entity.TokenSnapshot;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.user.service.TokenSnapshotService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Token Snapshot Scheduler Task
 * Captures daily token balance snapshots at 23:50 UTC
 */
@Component
@Slf4j
public class TokenSnapshotTask {
    @Autowired
    private TokenSnapshotService tokenSnapshotService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MarketFeign marketFeign;

    private static final int PAGE_SIZE = 500;

    /**
     * Daily token snapshot task
     */
    @XxlJob("dailyTokenSnapshotTask")
    public void dailyTokenSnapshotTask() {
        log.info("===>>> Starting daily token snapshot task");

        LocalDate snapshotDate = LocalDate.now(ZoneOffset.UTC);
        Map<String, BigDecimal> priceMap = loadPriceMap();

        long lastMemberId = 0L;
        long totalMembers = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            while (true) {
                List<Member> members = memberService.list(new LambdaQueryWrapper<Member>()
                        .select(Member::getId)
                        .gt(Member::getId, lastMemberId)
                        .orderByAsc(Member::getId)
                        .last("LIMIT " + PAGE_SIZE));

                if (members.isEmpty()) {
                    break;
                }

                lastMemberId = members.get(members.size() - 1).getId();
                totalMembers += members.size();

                List<Long> memberIds = members.stream().map(Member::getId).collect(Collectors.toList());
                List<MemberWallet> wallets = memberWalletService.list(new LambdaQueryWrapper<MemberWallet>()
                        .select(MemberWallet::getMemberId,
                                MemberWallet::getCoinId,
                                MemberWallet::getBalance,
                                MemberWallet::getFrozenBalance,
                                MemberWallet::getReleaseBalance)
                        .in(MemberWallet::getMemberId, memberIds)
                        .and(wrapper -> wrapper
                                .gt(MemberWallet::getBalance,
                                        BigDecimal.ZERO)
                                .or()
                                .gt(MemberWallet::getFrozenBalance,
                                        BigDecimal.ZERO)
                                .or()
                                .gt(MemberWallet::getReleaseBalance,
                                        BigDecimal.ZERO)));

                List<TokenSnapshot> snapshotBatch = new ArrayList<>();
                for (MemberWallet wallet : wallets) {
                    try {
                        BigDecimal balance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
                        BigDecimal frozenBalance = wallet.getFrozenBalance() != null ? wallet.getFrozenBalance()
                                : BigDecimal.ZERO;
                        BigDecimal releaseBalance = wallet.getReleaseBalance() != null ? wallet.getReleaseBalance()
                                : BigDecimal.ZERO;
                        BigDecimal totalQuantity = balance.add(frozenBalance).add(releaseBalance);

                        BigDecimal priceUsd = getPrice(wallet.getCoinId(), priceMap);
                        TokenSnapshot snapshot = new TokenSnapshot();
                        snapshot.setMemberId(wallet.getMemberId());
                        snapshot.setTokenSymbol(wallet.getCoinId());
                        snapshot.setSnapshotDate(snapshotDate);
                        snapshot.setSnapshotQuantity(totalQuantity);
                        snapshot.setSnapshotPrice(priceUsd);
                        snapshot.setSnapshotValue(totalQuantity.multiply(priceUsd));
                        snapshotBatch.add(snapshot);
                    } catch (Exception e) {
                        failCount++;
                        log.error("Error creating snapshot object for memberId={}, coin={}", wallet.getMemberId(),
                                wallet.getCoinId(), e);
                    }
                }

                if (!snapshotBatch.isEmpty()) {
                    try {
                        tokenSnapshotService.upsertBatchSnapshot(snapshotBatch);
                        successCount += snapshotBatch.size();
                    } catch (Exception e) {
                        failCount += snapshotBatch.size();
                        log.error("Error batch saving snapshots", e);
                    }
                }
                log.info("===>>> Processed batch: {} members, {} wallets (Success: {}, Failed: {})", members.size(),
                        wallets.size(), successCount, failCount);
            }
            log.info("===>>> Daily token snapshot completed. Total Members: {}, Success: {}, Failed: {}", totalMembers,
                    successCount, failCount);
        } catch (Exception e) {
            log.error("===>>> Daily token snapshot task failed", e);
        }
    }

    /**
     * Manual trigger
     */
    @XxlJob("manualTokenSnapshot")
    public void manualTokenSnapshot() {
        log.info("===>>> Manual token snapshot triggered");
        dailyTokenSnapshotTask();
    }

    private Map<String, BigDecimal> loadPriceMap() {
        List<CoinThumb> thumbs = marketFeign.findSymbolThumb4Feign();
        Map<String, BigDecimal> priceMap = new HashMap<>();
        priceMap.put("USDT", BigDecimal.ONE);

        if (thumbs != null) {
            for (CoinThumb thumb : thumbs) {
                String symbol = thumb.getSymbol().toUpperCase();
                BigDecimal close = thumb.getClose();

                if (symbol.endsWith("/USDT")) {
                    String coin = symbol.split("/")[0];
                    priceMap.put(coin, close);
                } else if (symbol.startsWith("USDT/")) {
                    // Reverse pair
                    String coin = symbol.split("/")[1];
                    if (close != null && close.compareTo(BigDecimal.ZERO) > 0) {
                        priceMap.put(coin, BigDecimal.ONE.divide(close, 8, RoundingMode.HALF_UP));
                    }
                }
            }
        }
        return priceMap;
    }

    private BigDecimal getPrice(String symbol, Map<String, BigDecimal> priceMap) {
        return priceMap.getOrDefault(symbol.toUpperCase(), BigDecimal.ZERO);
    }
}
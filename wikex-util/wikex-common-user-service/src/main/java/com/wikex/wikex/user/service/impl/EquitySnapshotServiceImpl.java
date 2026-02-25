package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.TransactionFlow;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.user.dto.EquityCalculateDTO;
import com.wikex.wikex.user.entity.EquitySnapshot;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.entity.TokenSnapshot;
import com.wikex.wikex.user.mapper.EquitySnapshotMapper;
import com.wikex.wikex.user.service.EquitySnapshotService;
import com.wikex.wikex.user.service.MemberTransactionService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.user.service.TokenSnapshotService;
import com.wikex.wikex.user.vo.SymbolAmountSum;
import com.wikex.wikex.user.vo.MemberSymbolAmountSum;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EquitySnapshotServiceImpl extends ServiceImpl<EquitySnapshotMapper, EquitySnapshot>
        implements EquitySnapshotService {

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private MarketFeign marketFeign;

    @Autowired
    private TokenSnapshotService tokenSnapshotService;

    @Override
    public Map<String, Object> calculateTodaySnapshot(Long memberId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);

        List<MemberWallet> wallets = memberWalletService.findAllByMemberId(memberId);
        Map<String, BigDecimal> priceMap = loadPriceMap();

        List<TokenSnapshot> snapshots = tokenSnapshotService.list(
                new LambdaQueryWrapper<TokenSnapshot>()
                        .eq(TokenSnapshot::getMemberId, memberId)
                        .eq(TokenSnapshot::getSnapshotDate, yesterday));

        Map<String, TokenSnapshot> snapshotMap = new HashMap<>();
        for (TokenSnapshot ts : snapshots) {
            if (ts.getTokenSymbol() != null) {
                snapshotMap.put(ts.getTokenSymbol(), ts);
            }
        }

        EquityCalculateDTO result = calculateEquity(wallets, priceMap, snapshotMap);

        // Net Inflow
        Date startTime = Date.from(today.atStartOfDay().toInstant(ZoneOffset.UTC));
        BigDecimal netInflowUsd = calculateTotalNetInflowUsd(memberId, startTime, priceMap);

        EquitySnapshot yEq = this.getOne(new LambdaQueryWrapper<EquitySnapshot>()
                .eq(EquitySnapshot::getMemberId, memberId)
                .eq(EquitySnapshot::getDate, yesterday), false);
        BigDecimal openValue = (yEq != null && yEq.getTotalEquity() != null) ? yEq.getTotalEquity() : BigDecimal.ZERO;

        BigDecimal totalPnl = result.getTotalEquity()
                .subtract(openValue)
                .subtract(netInflowUsd)
                .setScale(8, RoundingMode.HALF_UP);

        BigDecimal pnlPercent = null;
        if (openValue.compareTo(BigDecimal.ZERO) > 0) {
            pnlPercent = totalPnl
                    .multiply(BigDecimal.valueOf(100))
                    .divide(openValue, 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("totalEquity", result.getTotalEquity());
        resp.put("totalOpenValue", openValue);
        resp.put("totalPnL", totalPnl);
        resp.put("pnlPercent", pnlPercent);
        return resp;
    }

    @Override
    public void saveTodaySnapshotBatch(List<Long> memberIds, Map<String, BigDecimal> priceMap) {
        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);

        // Wallets
        Map<Long, List<MemberWallet>> walletMap = memberWalletService.list(
                new LambdaQueryWrapper<MemberWallet>()
                        .in(MemberWallet::getMemberId, memberIds))
                .stream().collect(
                        Collectors.groupingBy(MemberWallet::getMemberId));

        // Yesterday's snapshots
        Map<Long, Map<String, TokenSnapshot>> snapshotMap = tokenSnapshotService.list(
                new LambdaQueryWrapper<TokenSnapshot>()
                        .in(TokenSnapshot::getMemberId, memberIds)
                        .eq(TokenSnapshot::getSnapshotDate, yesterday))
                .stream().collect(Collectors.groupingBy(
                        TokenSnapshot::getMemberId,
                        Collectors.toMap(TokenSnapshot::getTokenSymbol, s -> s, (a, b) -> a)));

        // Existing equity snapshots for today
        Map<Long, EquitySnapshot> existingMap = this.list(
                new LambdaQueryWrapper<EquitySnapshot>()
                        .in(EquitySnapshot::getMemberId, memberIds)
                        .eq(EquitySnapshot::getDate, today))
                .stream()
                .collect(Collectors.toMap(EquitySnapshot::getMemberId, s -> s));

        List<EquitySnapshot> toSave = new ArrayList<>();

        Date startTime = Date.from(today.atStartOfDay().toInstant(ZoneOffset.UTC));
        List<MemberSymbolAmountSum> sumsBatch = memberTransactionService.sumAmountByMembersSince(memberIds, startTime);
        Map<Long, BigDecimal> netInflowMap = new HashMap<>();
        for (MemberSymbolAmountSum item : sumsBatch) {
            if (item.getTotalAmount() == null || item.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal price = getPrice(item.getSymbol(), priceMap);
            BigDecimal usdValue = item.getTotalAmount().multiply(price);
            TransactionType t = TransactionType.valueOfOrdinal(item.getType());
            if (t == null) {
                continue;
            }
            Long mid = item.getMemberId();
            BigDecimal acc = netInflowMap.getOrDefault(mid, BigDecimal.ZERO);
            if (TransactionFlow.isCashflowIn(t)) {
                acc = acc.add(usdValue);
            } else if (TransactionFlow.isCashflowOut(t)) {
                acc = acc.subtract(usdValue);
            }
            netInflowMap.put(mid, acc);
        }

        Map<Long, EquitySnapshot> yesterdayEquityMap = this.list(new LambdaQueryWrapper<EquitySnapshot>()
                .in(EquitySnapshot::getMemberId, memberIds)
                .eq(EquitySnapshot::getDate, yesterday))
                .stream().collect(
                        Collectors.toMap(EquitySnapshot::getMemberId, s -> s, (a, b) -> a));

        for (Long memberId : memberIds) {
            List<MemberWallet> wallets = walletMap.getOrDefault(memberId, new ArrayList<>());
            Map<String, TokenSnapshot> yesterdaySnapshots = snapshotMap.getOrDefault(memberId, Collections.emptyMap());

            EquityCalculateDTO result = calculateEquity(wallets, priceMap, yesterdaySnapshots);

            BigDecimal netInflowUsd = netInflowMap.getOrDefault(memberId, BigDecimal.ZERO)
                    .setScale(8, RoundingMode.HALF_UP);
            EquitySnapshot yEq = yesterdayEquityMap.get(memberId);
            BigDecimal openValue = (yEq != null && yEq.getTotalEquity() != null) ? yEq.getTotalEquity()
                    : BigDecimal.ZERO;
            BigDecimal totalPnl = result.getTotalEquity().subtract(openValue).subtract(netInflowUsd);

            EquitySnapshot snapshot = existingMap.getOrDefault(memberId, new EquitySnapshot());
            snapshot.setMemberId(memberId);
            snapshot.setDate(today);
            snapshot.setTotalEquity(result.getTotalEquity());
            snapshot.setTotalPnl(totalPnl);
            snapshot.setRealizedPnl(BigDecimal.ZERO);
            snapshot.setUnrealizedPnl(BigDecimal.ZERO);

            toSave.add(snapshot);
        }

        // Batch save
        if (!toSave.isEmpty()) {
            baseMapper.upsertBatch(toSave);
        }
    }

    @Override
    public List<Map<String, Object>> getEquityTrend(Long memberId, int days) {
        LocalDate endDate = LocalDate.now(TimeZone.getTimeZone("UTC").toZoneId()).minusDays(1);
        LocalDate startDate = endDate.minusDays(days - 1);

        LambdaQueryWrapper<EquitySnapshot> query = new LambdaQueryWrapper<>();
        query.eq(EquitySnapshot::getMemberId, memberId);
        query.ge(EquitySnapshot::getDate, startDate);
        query.le(EquitySnapshot::getDate, endDate);
        query.orderByAsc(EquitySnapshot::getDate);

        List<EquitySnapshot> list = this.list(query);
        Map<LocalDate, EquitySnapshot> map = list.stream()
                .collect(Collectors.toMap(EquitySnapshot::getDate, s -> s));

        List<Map<String, Object>> trend = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            EquitySnapshot s = map.get(date);

            BigDecimal totalEquity = BigDecimal.ZERO;
            BigDecimal totalPnl = BigDecimal.ZERO;
            BigDecimal pnlPercent = BigDecimal.ZERO;

            if (s != null) {
                totalEquity = Optional.ofNullable(s.getTotalEquity()).orElse(BigDecimal.ZERO);
                totalPnl = Optional.ofNullable(s.getTotalPnl()).orElse(BigDecimal.ZERO);

                BigDecimal prevEquity = totalEquity.subtract(totalPnl);
                if (prevEquity.compareTo(BigDecimal.ZERO) > 0) {
                    pnlPercent = totalPnl.multiply(BigDecimal.valueOf(100)).divide(prevEquity, 4, RoundingMode.HALF_UP);
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("date", date);
            item.put("totalAsset", totalEquity);
            item.put("totalPnL", totalPnl);
            item.put("pnlPercent", pnlPercent);

            trend.add(item);
        }
        return trend;
    }

    @Override
    public Map<String, Object> getSummary(Long memberId) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> todaySnapshot = calculateTodaySnapshot(memberId);
        BigDecimal todayAmount = todaySnapshot.get("totalPnL") instanceof BigDecimal
                ? (BigDecimal) todaySnapshot.get("totalPnL")
                : BigDecimal.ZERO;
        BigDecimal todayRate = todaySnapshot.get("pnlPercent") instanceof BigDecimal
                ? (BigDecimal) todaySnapshot.get("pnlPercent")
                : BigDecimal.ZERO;

        Map<String, Object> today = new HashMap<>();
        today.put("amount", todayAmount);
        today.put("rate", todayRate.setScale(2, RoundingMode.HALF_UP));

        result.put("today", today);

        result.put("day7", calculateSummary(memberId, 7));
        result.put("day30", calculateSummary(memberId, 30));
        return result;
    }

    @Override
    public Map<String, Object> getCumulativePnl(Long memberId, int days) {
        Map<String, Object> result = new HashMap<>();
        if (days <= 0) {
            result.put("startDate", null);
            result.put("endDate", null);
            result.put("items", new ArrayList<>());
            return result;
        }

        LocalDate now = LocalDate.now(TimeZone.getTimeZone("UTC").toZoneId());
        LocalDate startDate = now.minusDays(days - 1);
        LocalDate yesterday = now.minusDays(1);

        List<EquitySnapshot> snaps = this.list(new LambdaQueryWrapper<EquitySnapshot>()
                .eq(EquitySnapshot::getMemberId, memberId)
                .ge(EquitySnapshot::getDate, startDate)
                .le(EquitySnapshot::getDate, yesterday)
                .orderByAsc(EquitySnapshot::getDate));

        Map<LocalDate, BigDecimal> dailyMap = new HashMap<>();
        Map<LocalDate, BigDecimal> equityMap = new HashMap<>();
        for (EquitySnapshot s : snaps) {
            dailyMap.put(s.getDate(), Optional.ofNullable(s.getTotalPnl()).orElse(BigDecimal.ZERO));
            equityMap.put(s.getDate(), Optional.ofNullable(s.getTotalEquity()).orElse(BigDecimal.ZERO));
        }

        Map<String, Object> todaySnapshot = calculateTodaySnapshot(memberId);
        BigDecimal todayPnl = todaySnapshot.get("totalPnL") instanceof BigDecimal
                ? (BigDecimal) todaySnapshot.get("totalPnL")
                : BigDecimal.ZERO;
        BigDecimal todayEquity = todaySnapshot.get("totalEquity") instanceof BigDecimal
                ? (BigDecimal) todaySnapshot.get("totalEquity")
                : BigDecimal.ZERO;

        LocalDate baseDate = null;
        BigDecimal baseOpen = BigDecimal.ZERO;
        for (EquitySnapshot s : snaps) {
            BigDecimal eq = Optional.ofNullable(s.getTotalEquity()).orElse(BigDecimal.ZERO);
            if (eq.compareTo(BigDecimal.ZERO) > 0) {
                baseDate = s.getDate();
                baseOpen = eq;
                break;
            }
        }
        if (baseDate == null && todayEquity.compareTo(BigDecimal.ZERO) > 0) {
            baseDate = now;
            baseOpen = todayEquity;
        }

        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;

        LocalDate cur = startDate;
        while (!cur.isAfter(now)) {
            BigDecimal daily = cur.equals(now) ? todayPnl : dailyMap.getOrDefault(cur, BigDecimal.ZERO);
            cumulative = cumulative.add(daily).setScale(8, RoundingMode.HALF_UP);

            BigDecimal curEquity = cur.equals(now) ? todayEquity : equityMap.getOrDefault(cur, BigDecimal.ZERO);
            BigDecimal rate = BigDecimal.ZERO;
            if (baseDate != null && baseOpen.compareTo(BigDecimal.ZERO) > 0) {
                if (!cur.isBefore(baseDate) && curEquity.compareTo(BigDecimal.ZERO) > 0) {
                    if (cur.equals(baseDate)) {
                        rate = BigDecimal.ZERO;
                    } else {
                        rate = cumulative.multiply(BigDecimal.valueOf(100)).divide(baseOpen, 2, RoundingMode.HALF_UP);
                    }
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("date", cur);
            item.put("pnlAmount", cumulative);
            item.put("pnlRate", rate);

            items.add(item);
            cur = cur.plusDays(1);
        }

        result.put("startDate", startDate);
        result.put("endDate", now);
        result.put("items", items);

        return result;
    }

    @Override
    public List<Map<String, Object>> getDailyChart(Long memberId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<Map<String, Object>> equityTrendList = getEquityTrend(memberId, 6);
        for (Map<String, Object> trendItem : equityTrendList) {
            Map<String, Object> dailyItem = new HashMap<>();
            dailyItem.put("date", trendItem.get("date"));
            dailyItem.put("amount", trendItem.get("totalPnL"));
            result.add(dailyItem);
        }

        Map<String, Object> todaySnapshot = calculateTodaySnapshot(memberId);
        Map<String, Object> todayItem = new HashMap<>();
        todayItem.put("date", LocalDate.now(ZoneOffset.UTC));
        todayItem.put("amount", todaySnapshot.get("totalPnL"));
        result.add(todayItem);

        return result;
    }

    @Override
    public List<Map<String, Object>> getDailyCalendarMonth(Long memberId, int year, int month) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int targetYear = year > 0 ? year : today.getYear();
        int targetMonth = month > 0 ? month : today.getMonthValue();

        YearMonth targetYearMonth = YearMonth.of(targetYear, targetMonth);
        LocalDate startDate = targetYearMonth.atDay(1);
        LocalDate endDate = (targetYear == today.getYear() && targetMonth == today.getMonthValue()) ? today
                : targetYearMonth.atEndOfMonth();

        List<EquitySnapshot> snapshots = this.list(new LambdaQueryWrapper<EquitySnapshot>()
                .eq(EquitySnapshot::getMemberId, memberId)
                .ge(EquitySnapshot::getDate, startDate)
                .le(EquitySnapshot::getDate, endDate)
                .orderByAsc(EquitySnapshot::getDate));

        Map<LocalDate, BigDecimal> pnlByDate = new HashMap<>();
        for (EquitySnapshot snapshot : snapshots) {
            pnlByDate.put(snapshot.getDate(),
                    snapshot.getTotalPnl() != null ? snapshot.getTotalPnl() : BigDecimal.ZERO);
        }

        Map<String, Object> todaySnapshot = null;
        if (targetYear == today.getYear() && targetMonth == today.getMonthValue()) {
            todaySnapshot = calculateTodaySnapshot(memberId);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            BigDecimal dailyAmount;
            if (currentDate.equals(today) && todaySnapshot != null) {
                Object pnlValue = todaySnapshot.get("totalPnL");
                dailyAmount = todaySnapshot.get(
                        "totalPnL") instanceof BigDecimal ? (BigDecimal) pnlValue : BigDecimal.ZERO;
            } else {
                dailyAmount = pnlByDate.getOrDefault(currentDate, BigDecimal.ZERO);
            }

            Map<String, Object> dailyItem = new HashMap<>();
            dailyItem.put("date", currentDate);
            dailyItem.put("amount", dailyAmount);
            result.add(dailyItem);

            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getDailyCalendarYear(Long memberId, int year) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int targetYear = year > 0 ? year : today.getYear();

        LocalDate startDate = LocalDate.of(targetYear, 1, 1);
        LocalDate endDate = targetYear == today.getYear() ? today : LocalDate.of(targetYear, 12, 31);

        List<EquitySnapshot> snapshots = this.list(new LambdaQueryWrapper<EquitySnapshot>()
                .eq(EquitySnapshot::getMemberId, memberId)
                .ge(EquitySnapshot::getDate, startDate)
                .le(EquitySnapshot::getDate, endDate)
                .orderByAsc(EquitySnapshot::getDate));

        Map<YearMonth, BigDecimal> pnlByMonth = new HashMap<>();
        for (EquitySnapshot snapshot : snapshots) {
            YearMonth snapshotMonth = YearMonth.of(snapshot.getDate().getYear(), snapshot.getDate().getMonthValue());

            BigDecimal currentAmount = pnlByMonth.getOrDefault(snapshotMonth, BigDecimal.ZERO);
            BigDecimal addAmount = snapshot.getTotalPnl() != null ? snapshot.getTotalPnl() : BigDecimal.ZERO;
            pnlByMonth.put(snapshotMonth, currentAmount.add(addAmount));
        }

        if (targetYear == today.getYear()) {
            Map<String, Object> todaySnapshot = calculateTodaySnapshot(memberId);
            Object pnlValue = todaySnapshot.get("totalPnL");
            BigDecimal todayAmount = pnlValue instanceof BigDecimal ? (BigDecimal) pnlValue : BigDecimal.ZERO;

            YearMonth currentMonth = YearMonth.from(today);
            pnlByMonth.put(currentMonth, pnlByMonth.getOrDefault(currentMonth, BigDecimal.ZERO).add(todayAmount));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        YearMonth currentMonth = YearMonth.of(targetYear, 1);
        YearMonth lastMonth = YearMonth.from(endDate);

        while (!currentMonth.isAfter(lastMonth)) {
            Map<String, Object> monthlyItem = new HashMap<>();
            monthlyItem.put("period", currentMonth.toString());
            monthlyItem.put("amount", pnlByMonth.getOrDefault(currentMonth, BigDecimal.ZERO));
            result.add(monthlyItem);
            currentMonth = currentMonth.plusMonths(1);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getSpotEquityTrend(Long memberId, int days) {
        int historyDays = Math.max(0, days - 1);

        List<Map<String, Object>> equityTrendHistory = getEquityTrend(memberId, historyDays);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> historyItem : equityTrendHistory) {

            Map<String, Object> dailyItem = new HashMap<>();
            dailyItem.put("date", historyItem.get("date"));
            dailyItem.put("amount", historyItem.get("totalAsset"));

            result.add(dailyItem);
        }

        Map<String, Object> todaySnapshot = calculateTodaySnapshot(memberId);

        Map<String, Object> todayItem = new HashMap<>();
        todayItem.put("date", LocalDate.now(ZoneOffset.UTC));
        todayItem.put("amount", todaySnapshot.get("totalEquity"));

        result.add(todayItem);
        return result;
    }

    @Override
    public Map<String, Object> getSpotAssetBreakdown(Long memberId, int limitTop) {
        List<MemberWallet> memberWallets = memberWalletService.findAllByMemberId(memberId);

        Map<String, BigDecimal> latestPriceMap = loadPriceMap();

        List<Map<String, Object>> assetValueList = new ArrayList<>();
        BigDecimal totalAssetValue = BigDecimal.ZERO;

        for (MemberWallet wallet : memberWallets) {
            String symbol = wallet.getCoinId();
            if (symbol == null || symbol.isEmpty()) {
                continue;
            }

            BigDecimal balance = Optional.ofNullable(wallet.getBalance()).orElse(BigDecimal.ZERO);
            BigDecimal frozen = Optional.ofNullable(wallet.getFrozenBalance()).orElse(BigDecimal.ZERO);
            BigDecimal release = Optional.ofNullable(wallet.getReleaseBalance()).orElse(BigDecimal.ZERO);

            BigDecimal total = balance.add(frozen).add(release);
            if (total.signum() <= 0) {
                continue;
            }

            BigDecimal price = getPrice(symbol, latestPriceMap);
            if (price.signum() <= 0) {
                continue;
            }

            BigDecimal assetValue = total.multiply(price).setScale(8, RoundingMode.HALF_UP);
            totalAssetValue = totalAssetValue.add(assetValue);

            Map<String, Object> assetItem = new HashMap<>();
            assetItem.put("symbol", symbol);
            assetItem.put("amount", assetValue);

            assetValueList.add(assetItem);
        }

        assetValueList.sort((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")));

        int displayLimit = Math.max(1, limitTop);

        List<Map<String, Object>> breakdownItems = new ArrayList<>();
        BigDecimal othersValue = BigDecimal.ZERO;

        for (int i = 0; i < assetValueList.size(); i++) {
            Map<String, Object> asset = assetValueList.get(i);
            BigDecimal amount = (BigDecimal) asset.get("amount");

            if (i < displayLimit) {
                BigDecimal percent = BigDecimal.ZERO;
                if (totalAssetValue.signum() > 0) {
                    percent = amount.multiply(BigDecimal.valueOf(100)).divide(totalAssetValue, 2, RoundingMode.HALF_UP);
                }

                asset.put("percent", percent);
                breakdownItems.add(asset);
            } else {
                othersValue = othersValue.add(amount);
            }
        }

        if (othersValue.signum() > 0) {
            BigDecimal percent = BigDecimal.ZERO;
            if (totalAssetValue.signum() > 0) {
                percent = othersValue.multiply(BigDecimal.valueOf(100))
                        .divide(totalAssetValue, 2, RoundingMode.HALF_UP);
            }

            Map<String, Object> othersItem = new HashMap<>();
            othersItem.put("symbol", "Others");
            othersItem.put("amount", othersValue.setScale(8, RoundingMode.HALF_UP));
            othersItem.put("percent", percent);

            breakdownItems.add(othersItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", totalAssetValue.setScale(8, RoundingMode.HALF_UP));
        result.put("items", breakdownItems);

        return result;
    }

    private EquityCalculateDTO calculateEquity(List<MemberWallet> wallets, Map<String, BigDecimal> priceMap,
            Map<String, TokenSnapshot> yesterdaySnapshots) {
        BigDecimal totalOpenValue = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (MemberWallet wallet : wallets) {
            String symbol = wallet.getCoinId();
            if (symbol == null || symbol.isEmpty()) {
                continue;
            }

            BigDecimal balance = wallet.getBalance() != null ? wallet.getBalance() : BigDecimal.ZERO;
            BigDecimal frozen = wallet.getFrozenBalance() != null ? wallet.getFrozenBalance() : BigDecimal.ZERO;
            BigDecimal release = wallet.getReleaseBalance() != null ? wallet.getReleaseBalance() : BigDecimal.ZERO;
            BigDecimal qty = balance.add(frozen).add(release);
            if (qty.signum() == 0) {
                continue;
            }

            BigDecimal price = getPrice(symbol, priceMap);
            if (price.signum() <= 0) {
                continue;
            }

            BigDecimal currentValue = qty.multiply(price).setScale(8, RoundingMode.HALF_UP);
            totalCurrentValue = totalCurrentValue.add(currentValue);

            TokenSnapshot ts = yesterdaySnapshots.get(symbol);
            if (ts != null && ts.getSnapshotPrice() != null && ts.getSnapshotQuantity() != null) {
                totalOpenValue = totalOpenValue.add(
                        ts.getSnapshotQuantity().multiply(ts.getSnapshotPrice()).setScale(8, RoundingMode.HALF_UP));
            }
        }

        BigDecimal totalPnl = totalCurrentValue.subtract(totalOpenValue).setScale(8, RoundingMode.HALF_UP);

        return new EquityCalculateDTO(totalCurrentValue, totalOpenValue, totalPnl);
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
                priceMap.put(
                        symbol.split("/")[1],
                        BigDecimal.ONE.divide(close, 8, RoundingMode.HALF_UP));
            }
        }
        return priceMap;
    }

    private BigDecimal getPrice(String symbol, Map<String, BigDecimal> priceMap) {
        return priceMap.getOrDefault(symbol.toUpperCase(), BigDecimal.ZERO);
    }

    private BigDecimal calculateTotalNetInflowUsd(Long memberId, Date startTime, Map<String, BigDecimal> priceMap) {
        List<SymbolAmountSum> sums = memberTransactionService.sumAmountByMemberSince(memberId, startTime);
        BigDecimal total = BigDecimal.ZERO;

        for (SymbolAmountSum item : sums) {
            BigDecimal amount = item.getTotalAmount();
            if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            String symbol = item.getSymbol();
            BigDecimal price = getPrice(symbol, priceMap);

            BigDecimal usdValue = amount.multiply(price);

            TransactionType t = TransactionType.valueOfOrdinal(item.getType());
            if (t == null) {
                continue;
            }

            if (TransactionFlow.isCashflowIn(t)) {
                total = total.add(usdValue);
            } else if (TransactionFlow.isCashflowOut(t)) {
                total = total.subtract(usdValue);
            }
        }
        return total.setScale(8, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateInclusiveTotalPnl(Long memberId, int days) {
        if (days <= 0) {
            return BigDecimal.ZERO;
        }

        Map<String, Object> todaySnapshot = calculateTodaySnapshot(memberId);
        BigDecimal todayPnl = todaySnapshot.get("totalPnL") instanceof BigDecimal
                ? (BigDecimal) todaySnapshot.get("totalPnL")
                : BigDecimal.ZERO;

        if (days == 1) {
            return todayPnl;
        }

        LocalDate now = LocalDate.now(TimeZone.getTimeZone("UTC").toZoneId());
        LocalDate endDate = now.minusDays(1);
        LocalDate startDate = endDate.minusDays(days - 2);

        List<EquitySnapshot> list = this.list(new LambdaQueryWrapper<EquitySnapshot>()
                .eq(EquitySnapshot::getMemberId, memberId)
                .ge(EquitySnapshot::getDate, startDate)
                .le(EquitySnapshot::getDate, endDate));

        BigDecimal total = BigDecimal.ZERO;
        for (EquitySnapshot s : list) {
            total = total.add(Optional.ofNullable(s.getTotalPnl()).orElse(BigDecimal.ZERO));
        }

        return total.add(todayPnl);
    }

    private Map<String, Object> calculateSummary(Long memberId, int days) {
        LocalDate now = LocalDate.now(TimeZone.getTimeZone("UTC").toZoneId());
        LocalDate start = now.minusDays(days - 1);

        BigDecimal amount = calculateInclusiveTotalPnl(memberId, days);
        BigDecimal rate = BigDecimal.ZERO;

        List<EquitySnapshot> snapshots = this.list(new LambdaQueryWrapper<EquitySnapshot>()
                .eq(EquitySnapshot::getMemberId, memberId)
                .ge(EquitySnapshot::getDate, start)
                .le(EquitySnapshot::getDate, now)
                .orderByAsc(EquitySnapshot::getDate));

        EquitySnapshot base = null;
        for (EquitySnapshot s : snapshots) {
            BigDecimal eq = Optional.ofNullable(s.getTotalEquity()).orElse(BigDecimal.ZERO);
            BigDecimal pnl = Optional.ofNullable(s.getTotalPnl()).orElse(BigDecimal.ZERO);
            BigDecimal open = eq.subtract(pnl);
            if (open.compareTo(BigDecimal.ZERO) > 0) {
                base = s;
                break;
            }
        }

        if (base != null) {
            BigDecimal baseOpen = Optional.ofNullable(base.getTotalEquity()).orElse(BigDecimal.ZERO)
                    .subtract(Optional.ofNullable(base.getTotalPnl()).orElse(BigDecimal.ZERO));

            if (baseOpen.compareTo(BigDecimal.ZERO) > 0) {
                rate = amount.multiply(BigDecimal.valueOf(100)).divide(baseOpen, 2, RoundingMode.HALF_UP);
            }
        }

        Map<String, Object> item = new HashMap<>();
        item.put("amount", amount);
        item.put("rate", rate.setScale(2, RoundingMode.HALF_UP));
        return item;
    }
}
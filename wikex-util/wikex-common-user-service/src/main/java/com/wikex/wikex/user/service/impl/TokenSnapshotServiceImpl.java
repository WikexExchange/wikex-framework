package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.TransactionFlow;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.entity.TokenSnapshot;
import com.wikex.wikex.user.mapper.TokenSnapshotMapper;
import com.wikex.wikex.user.service.MemberTransactionService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.user.service.TokenSnapshotService;
import com.wikex.wikex.user.vo.SymbolAmountSum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenSnapshotServiceImpl extends ServiceImpl<TokenSnapshotMapper, TokenSnapshot>
        implements TokenSnapshotService {

    @Autowired
    private MemberWalletService memberWalletService;

    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private MarketFeign marketFeign;

    @Autowired
    private TokenSnapshotMapper tokenSnapshotMapper;

    @Override
    public TokenSnapshot findSnapshot(Long memberId, String symbol, LocalDate date) {
        LambdaQueryWrapper<TokenSnapshot> query = new LambdaQueryWrapper<>();
        query.eq(TokenSnapshot::getMemberId, memberId)
                .eq(TokenSnapshot::getTokenSymbol, symbol)
                .eq(TokenSnapshot::getSnapshotDate, date);
        return this.getOne(query, false);
    }

    @Override
    public void upsertBatchSnapshot(List<TokenSnapshot> snapshotList) {
        if (snapshotList == null || snapshotList.isEmpty()) {
            return;
        }
        tokenSnapshotMapper.upsertBatch(snapshotList);
    }

    @Override
    public Map<String, Object> calculateTodayTokenPnl(Long memberId, String symbol) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);

        // Current quantity
        MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(symbol, memberId);
        BigDecimal balance = wallet != null ? wallet.getBalance() : BigDecimal.ZERO;
        BigDecimal frozen = wallet != null ? wallet.getFrozenBalance() : BigDecimal.ZERO;
        BigDecimal release = wallet != null ? wallet.getReleaseBalance() : BigDecimal.ZERO;

        BigDecimal currentQty = balance.add(frozen).add(release);

        // Current price
        BigDecimal currentPriceUsd = getLastPriceToUsdt(symbol);

        // Snapshot at 23:50
        TokenSnapshot snapshot = findSnapshot(memberId, symbol, yesterday);
        BigDecimal openQty = snapshot != null ? snapshot.getSnapshotQuantity() : BigDecimal.ZERO;
        BigDecimal openPriceUsd = snapshot != null ? snapshot.getSnapshotPrice() : BigDecimal.ZERO;

        // Net Inflow
        BigDecimal netInflowUsd = calculateNetInflowUsd(memberId, today, symbol, currentPriceUsd);

        BigDecimal pnl = currentQty.multiply(currentPriceUsd)
                .subtract(openQty.multiply(openPriceUsd))
                .subtract(netInflowUsd)
                .setScale(8, RoundingMode.HALF_UP);

        Map<String, Object> ret = new HashMap<>();
        ret.put("tokenSymbol", symbol);
        ret.put("currentQty", currentQty);
        ret.put("currentPriceUsd", currentPriceUsd);
        ret.put("netInflowUsd", netInflowUsd);
        ret.put("pnlUsd", pnl);
        return ret;
    }

    private BigDecimal calculateNetInflowUsd(Long memberId, LocalDate date, String symbol, BigDecimal currentPrice) {
        Date start = Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC));
        List<SymbolAmountSum> list = memberTransactionService.sumAmountByMemberSince(memberId, start);

        BigDecimal total = BigDecimal.ZERO;
        for (SymbolAmountSum s : list) {
            if (symbol != null && !symbol.equalsIgnoreCase(s.getSymbol())) {
                continue;
            }
            TransactionType type = TransactionType.valueOfOrdinal(s.getType());
            // Only consider CashFlow (Deposit/Withdrawal)
            if (type == null || !TransactionFlow.isCashFlow(type)) {
                continue;
            }

            BigDecimal amount = s.getTotalAmount() == null ? BigDecimal.ZERO : s.getTotalAmount();
            BigDecimal valUsd = amount.multiply(currentPrice);

            if (TransactionFlow.isCashflowIn(type)) {
                total = total.add(valUsd);
            } else if (TransactionFlow.isCashflowOut(type)) {
                total = total.subtract(valUsd);
            }
        }
        return total.setScale(8, RoundingMode.HALF_UP);
    }

    private BigDecimal getLastPriceToUsdt(String fromUnit) {
        if ("USDT".equalsIgnoreCase(fromUnit)) {
            return BigDecimal.ONE;
        }

        String base = fromUnit.toUpperCase();
        String pair = base + "/USDT";
        String reversePair = "USDT/" + base;

        List<CoinThumb> thumbs = marketFeign.findSymbolThumb4Feign();
        if (thumbs == null) {
            return BigDecimal.ZERO;
        }

        for (CoinThumb thumb : thumbs) {
            if (thumb.getSymbol() == null || thumb.getClose() == null) {
                continue;
            }
            if (pair.equalsIgnoreCase(thumb.getSymbol())) {
                return thumb.getClose();
            }
            if (reversePair.equalsIgnoreCase(thumb.getSymbol()) && thumb.getClose().signum() > 0) {
                return BigDecimal.ONE.divide(thumb.getClose(), 8, RoundingMode.HALF_UP);
            }
        }

        return BigDecimal.ZERO;
    }
}

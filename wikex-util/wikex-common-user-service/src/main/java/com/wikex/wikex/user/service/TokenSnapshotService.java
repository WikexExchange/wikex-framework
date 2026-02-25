package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.TokenSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TokenSnapshotService extends IService<TokenSnapshot> {
    TokenSnapshot findSnapshot(Long memberId, String symbol, LocalDate date);

    void upsertBatchSnapshot(List<TokenSnapshot> snapshotList);

    Map<String, Object> calculateTodayTokenPnl(Long memberId, String symbol);
}

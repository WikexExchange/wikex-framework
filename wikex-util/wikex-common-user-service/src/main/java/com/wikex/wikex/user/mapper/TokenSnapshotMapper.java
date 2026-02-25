package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.TokenSnapshot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TokenSnapshotMapper extends BaseMapper<TokenSnapshot> {
    int upsertBatch(@Param("list") List<TokenSnapshot> list);
}

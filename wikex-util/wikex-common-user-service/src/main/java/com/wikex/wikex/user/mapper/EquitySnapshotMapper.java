package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.EquitySnapshot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EquitySnapshotMapper extends BaseMapper<EquitySnapshot> {
    int upsertBatch(@Param("list") List<EquitySnapshot> list);
}
package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.RewardPeriodType;
import com.wikex.wikex.user.mapper.RewardPeriodTypeMapper;
import com.wikex.wikex.user.service.RewardPeriodTypeService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RewardPeriodTypeServiceImpl extends ServiceImpl<RewardPeriodTypeMapper, RewardPeriodType>
        implements RewardPeriodTypeService {
    @Override
    public RewardPeriodType findOne(String period, Integer yearNo, Integer periodNo) {
        QueryWrapper<RewardPeriodType> query = new QueryWrapper<>();
        query.eq("period", period)
                .eq("year_no", yearNo)
                .eq("period_no", periodNo)
                .last("limit 1");
        return this.getOne(query);
    }

    @Override
    public List<RewardPeriodType> findAll(String period, Integer yearNo, Integer periodNo) {
        QueryWrapper<RewardPeriodType> query = new QueryWrapper<>();
        query.eq("period", period)
                .eq("year_no", yearNo)
                .eq("period_no", periodNo);
        return this.list(query);
    }

    @Override
    public List<RewardPeriodType> findAllByPeriodAndPeriodNo(String period, Integer periodNo) {
        QueryWrapper<RewardPeriodType> query = new QueryWrapper<>();
        query.eq("period", period)
                .eq("period_no", periodNo);
        return this.list(query);
    }
}

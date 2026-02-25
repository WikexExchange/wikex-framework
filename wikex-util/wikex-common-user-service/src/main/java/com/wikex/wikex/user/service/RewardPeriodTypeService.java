package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.RewardPeriodType;
import java.util.List;

public interface RewardPeriodTypeService extends IService<RewardPeriodType> {
    RewardPeriodType findOne(String period, Integer yearNo, Integer periodNo);

    List<RewardPeriodType> findAll(String period, Integer yearNo, Integer periodNo);

    List<RewardPeriodType> findAllByPeriodAndPeriodNo(String period, Integer periodNo);
}

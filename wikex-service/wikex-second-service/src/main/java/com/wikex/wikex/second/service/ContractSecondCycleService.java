package com.wikex.wikex.second.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCycle;
import com.baomidou.mybatisplus.extension.service.IService;


public interface ContractSecondCycleService extends IService<ContractSecondCycle> {

    ContractSecondCycle findOne(Long cycleId);

    Page<ContractSecondCycle> findAll(PageParam pageParam);
}

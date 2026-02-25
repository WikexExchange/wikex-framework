package com.wikex.wikex.second.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondSet;


public interface ContractSecondSetService extends IService<ContractSecondSet> {

    ContractSecondSet findSetByTime(String h);

    Page<ContractSecondSet> findAll(PageParam pageParam);
}

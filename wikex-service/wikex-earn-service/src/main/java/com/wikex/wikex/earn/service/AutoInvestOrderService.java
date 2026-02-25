package com.wikex.wikex.earn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.earn.entity.AutoInvestOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.PageParam;


public interface AutoInvestOrderService extends IService<AutoInvestOrder> {

    IPage<AutoInvestOrder> findAll(Long memberId, PageParam pageParam);
}

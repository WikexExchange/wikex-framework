package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.ConvertOrderScreen;
import com.wikex.wikex.user.entity.ConvertOrder;



public interface ConvertOrderService extends IService<ConvertOrder> {


    IPage<ConvertOrder> queryByMember(Long memberId, int pageNo, int pageSize);

    Page<ConvertOrder> findAll(ConvertOrderScreen orderScreen);
}

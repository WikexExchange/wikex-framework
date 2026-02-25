package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.QuickExchange;

import java.util.List;


public interface QuickExchangeService extends IService<QuickExchange> {

    List<QuickExchange> findAllByMemberId(Long memberId);
}

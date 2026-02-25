package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.Automainconfig;


public interface AutomainconfigService extends IService<Automainconfig> {

    Page<Automainconfig> findAll(Integer pageNo, Integer pageSize);

    Automainconfig findAutoMainConfigByCoinNameAndProtocol(String coinName, Integer protocol);
}

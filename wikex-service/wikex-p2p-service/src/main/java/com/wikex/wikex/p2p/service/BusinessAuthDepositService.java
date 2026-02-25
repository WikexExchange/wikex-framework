package com.wikex.wikex.p2p.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.p2p.entity.BusinessAuthDeposit;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface BusinessAuthDepositService extends IService<BusinessAuthDeposit> {

    List<BusinessAuthDeposit> findAllByStatus(CommonStatus normal);

    Page<BusinessAuthDeposit> findAll(Integer pageNo,Integer pageSize, CommonStatus status);
}

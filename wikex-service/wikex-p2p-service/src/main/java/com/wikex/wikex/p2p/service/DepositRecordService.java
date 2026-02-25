package com.wikex.wikex.p2p.service;

import com.wikex.wikex.constant.DepositStatusEnum;
import com.wikex.wikex.p2p.entity.DepositRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface DepositRecordService extends IService<DepositRecord> {

    List<DepositRecord> findDepositByMemberAndStatus(Long memberId, DepositStatusEnum status);
}

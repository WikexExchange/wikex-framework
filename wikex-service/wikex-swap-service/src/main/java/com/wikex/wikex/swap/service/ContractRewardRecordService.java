package com.wikex.wikex.swap.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.wikex.wikex.swap.entity.ContractRewardRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.swap.vo.RewardSetVo;


public interface ContractRewardRecordService extends IService<ContractRewardRecord> {

    void sendReward(ContractOrderEntrust orderEntrust);

    Page<ContractRewardRecord> findAll(ContractRewardRecordScreen screen);

    RewardSetVo findAllRewardSetVo();

    void clearAllRewardSetVo();

    void clearRewardSetVoById(Long memberId);
}

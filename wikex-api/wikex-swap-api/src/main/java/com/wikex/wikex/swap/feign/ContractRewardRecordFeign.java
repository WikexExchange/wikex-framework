package com.wikex.wikex.swap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.swap.entity.ContractRewardRecord;
import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-swap",contextId = "contractRewardFeign")
public interface ContractRewardRecordFeign {

    @PostMapping(value = "/contractRewardRecordFeign/page-query")
    Page<ContractRewardRecord> findAll(@RequestBody ContractRewardRecordScreen screen);

    @PostMapping(value = "/contractRewardRecordFeign/rewardSets")
    RewardSetVo findAllRewardSetVo();

    @PostMapping(value = "/contractRewardRecordFeign/clearAllRewardSetVo")
    MessageResult clearAllRewardSetVo();

    @PostMapping(value = "/contractRewardRecordFeign/clearRewardSetVoById")
    void clearRewardSetVoById(@RequestParam("memberId") Long memberId);
}

package com.wikex.wikex.swap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.swap.entity.ContractRewardRecord;
import com.wikex.wikex.swap.service.ContractRewardRecordService;
import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Contract Rebate Records")
@RestController
@RequestMapping("/contractRewardRecordFeign")
@Slf4j
public class ContractRewardRecordFeignController extends BaseController {
    @Autowired
    private ContractRewardRecordService contractRewardRecordService;
    @Autowired
    private MemberFeign memberFeign;

    /**
     * Paginated query
     * @param screen
     * @return
     */
    @PostMapping("page-query")
    public Page<ContractRewardRecord> findAll(ContractRewardRecordScreen screen) {
        // Get query conditions
        Page<ContractRewardRecord> all = contractRewardRecordService.findAll(screen);
        return all;
    }

    /**
     * Query rebate settings
     * @return
     */
    @PostMapping("rewardSets")
    public RewardSetVo findAllRewardSetVo() {
        // Get query conditions
        RewardSetVo vo = contractRewardRecordService.findAllRewardSetVo();
        return vo;
    }

    /**
     * Clear rebate settings cache
     * @return
     */
    @PostMapping("clearAllRewardSetVo")
    public MessageResult clearAllRewardSetVo() {
        // Get query conditions
        contractRewardRecordService.clearAllRewardSetVo();
        return success();
    }

    @RequestMapping(value = "clearRewardSetVoById")
    public MessageResult clearRewardSetVoById(@RequestParam("memberId") Long memberId){
        Member checkMember = memberFeign.findMemberById(memberId);
        if(!checkMember.getSuperPartner().equals("1")) {
            return error("You are not an agent!");
        }
        contractRewardRecordService.clearRewardSetVoById(memberId);
        return success();
    }

}

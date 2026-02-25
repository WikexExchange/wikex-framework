package com.wikex.wikex.admin.controller.swap;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.swap.entity.ContractRewardRecord;
import com.wikex.wikex.swap.feign.ContractRewardRecordFeign;
import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/swap/reward")
@Slf4j
public class ContractRewardRecordController extends BaseAdminController {
    @Autowired
    private ContractRewardRecordFeign contractRewardRecordService;

    /**
     * Paginated query
     * @param screen
     * @return
     */
    @RequiresPermissions("swap:reward:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Perpetual contract rebate orders list")
    public MessageResult pageQuery(ContractRewardRecordScreen screen) {
        // Get query conditions
        Page<ContractRewardRecord> all = contractRewardRecordService.findAll(screen);
        return success(IPage2Page(all));
    }

    /**
     * Rebate settings query
     * @return
     */
    @RequiresPermissions("swap:reward:rewardSets")
    @PostMapping("rewardSets")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Rebate settings query")
    public MessageResult rewardSets() {
        // Get query conditions
        RewardSetVo vo = contractRewardRecordService.findAllRewardSetVo();
        return success(vo);
    }

    /**
     * Clear rebate settings cache
     * @return
     */
    @RequiresPermissions("swap:reward:clear")
    @PostMapping("clear")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Clear rebate settings cache")
    public MessageResult clear() {
        // Get query conditions
        contractRewardRecordService.clearAllRewardSetVo();
        return success();
    }

}

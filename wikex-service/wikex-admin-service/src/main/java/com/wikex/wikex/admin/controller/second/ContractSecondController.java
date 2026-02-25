package com.wikex.wikex.admin.controller.second;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.ContractFinanceScreen;
import com.wikex.wikex.screen.ContractSecondOrderScreen;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCycle;
import com.wikex.wikex.second.entity.ContractSecondOrder;
import com.wikex.wikex.second.entity.ContractSecondSet;
import com.wikex.wikex.second.feign.ContractSecondCycleFeign;
import com.wikex.wikex.second.feign.ContractSecondOrderFeign;
import com.wikex.wikex.second.feign.ContractSecondSetFeign;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.MemberSecondWallet;
import com.wikex.wikex.user.feign.MemberSecondWalletFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/second")
@Slf4j
public class ContractSecondController extends BaseAdminController {
    @Autowired
    private ContractSecondSetFeign contractSecondSetService;
    @Autowired
    private ContractSecondCycleFeign contractSecondCycleService;
    @Autowired
    private ContractSecondOrderFeign contractSecondOrderService;
    @Autowired
    private MemberSecondWalletFeign memberSecondWalletService;
    @Autowired
    private LocaleMessageSourceService messageSource;


    /**
     * Query orders
     * @param screen
     * @return
     */
    @RequiresPermissions("second-order:page-query")
    @PostMapping("order/page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Second contract order list")
    public MessageResult queryOrders(ContractSecondOrderScreen screen) {
        Page<ContractSecondOrder> all = contractSecondOrderService.findAll(screen);
        return success(IPage2Page(all));
    }
    /**
     * Query compensation settings
     * @param pageParam
     * @return
     */
    @RequiresPermissions("set:page-query")
    @PostMapping("set/page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Compensation setting list")
    public MessageResult querySets(PageParam pageParam) {
        Page<ContractSecondSet> all = contractSecondSetService.findSecondSetAll(pageParam);
        return success(IPage2Page(all));
    }


    /**
     * Add compensation setting
     * @param contractSecondSet
     * @return
     */
    @RequiresPermissions("second-set:add")
    @PostMapping("set/add")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Add compensation setting")
    public MessageResult add(@Valid ContractSecondSet contractSecondSet) {
        contractSecondSet.setCreateTime(new Date());
        contractSecondSet.setUpdateTime(new Date());
        contractSecondSet = contractSecondSetService.save(contractSecondSet);
        return MessageResult.getSuccessInstance("Added successfully!", contractSecondSet);
    }

    /**
     * Modify compensation setting
     * @return
     */
    @RequiresPermissions("second-set:alter")
    @PostMapping("set/alter")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Modify compensation setting")
    public MessageResult alter(
            @RequestParam("id") Long id,
            @RequestParam(value = "startTime", required = false) String startTime, // Start time
            @RequestParam(value = "endTime", required = false) String endTime, // End time
            @RequestParam(value = "orderNum", required = false) Integer orderNum, // Number of compensated orders
            @RequestParam(value = "limitRate", required = false) BigDecimal limitRate
    ) {
        ContractSecondSet set = contractSecondSetService.findOne(id);
        if(set == null) {
            return error(messageSource.getMessage("STOP_LOSS") + id + messageSource.getMessage("NOT_FOUND"));
        }
        if(startTime != null) set.setStartTime(startTime);
        if(endTime != null) set.setEndTime(endTime);
        if(orderNum != null) set.setOrderNum(orderNum);
        if(limitRate != null) set.setLimitRate(limitRate);
        set.setUpdateTime(new Date());
        contractSecondSetService.save(set);
        return success(messageSource.getMessage("SAVE_SUCCESS"));
    }

    /**
     * Delete compensation setting
     * @return
     */
    @RequiresPermissions("second-set:del")
    @PostMapping("set/delete")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Delete compensation setting")
    public MessageResult delete(
            @RequestParam(value = "ids") String[] ids
    ) {
        List<Long> delIds = new ArrayList<>();
        if(ids!=null && ids.length>0){
            for (String id : ids) {
                delIds.add(Long.parseLong(id));
            }
        }
        contractSecondSetService.deleteBatch(delIds);
        return success();
    }


    /**
     * Query contract cycles
     * @param pageParam
     * @return
     */
    @RequiresPermissions("cycle:page-query")
    @PostMapping("cycle/page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Second contract cycle list")
    public MessageResult queryCycles(PageParam pageParam) {
        Page<ContractSecondCycle> all = contractSecondCycleService.findAll(pageParam);
        return success(IPage2Page(all));
    }

    /**
     * Add second contract cycle
     * @param contractSecondCycle
     * @return
     */
    @RequiresPermissions("second-cycle:add")
    @PostMapping("cycle/add")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Add second contract cycle")
    public MessageResult addCycle(@Valid ContractSecondCycle contractSecondCycle) {
        contractSecondCycle.setCreateTime(new Date());
        contractSecondCycle.setUpdateTime(new Date());
        contractSecondCycle = contractSecondCycleService.save(contractSecondCycle);
        return MessageResult.getSuccessInstance(messageSource.getMessage("ADD_SUCCESS"), contractSecondCycle);
    }

    /**
     * Modify second contract cycle
     * @return
     */
    @RequiresPermissions("second-cycle:alter")
    @PostMapping("cycle/alter")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Modify second contract cycle")
    public MessageResult alterCycle(
            @RequestParam("id") Long id,
            @RequestParam(value = "cycleRate", required = false) BigDecimal cycleRate, // Cycle odds
            @RequestParam(value = "cycleLength", required = false) Long cycleLength, // Cycle duration (seconds)
            @RequestParam(value = "minAmount", required = false) BigDecimal minAmount, // Minimum amount
            @RequestParam(value = "maxAmount", required = false) BigDecimal maxAmount // Maximum amount
    ) {
        ContractSecondCycle cycle = contractSecondCycleService.findOne(id);
        if(cycle == null) {
            return error(messageSource.getMessage("MODIFY_FUTURES_CONTRACT_PERIOD") + id + messageSource.getMessage("NOT_FOUND"));
        }
        if(cycleRate != null) cycle.setCycleRate(cycleRate);
        if(cycleLength != null) cycle.setCycleLength(cycleLength);
        if(minAmount != null) cycle.setMinAmount(minAmount);
        if(maxAmount != null) cycle.setMaxAmount(maxAmount);
        cycle.setUpdateTime(new Date());
        contractSecondCycleService.save(cycle);
        return success(messageSource.getMessage("SAVE_SUCCESS"));
    }

    /**
     * Delete second contract cycle
     * @return
     */
    @RequiresPermissions("second-cycle:del")
    @PostMapping("cycle/delete")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Delete second contract cycle")
    public MessageResult delCycle(@RequestParam(value = "ids") String[] ids) {
        List<Long> delIds = new ArrayList<>();
        if(ids!=null && ids.length>0){
            for (String id : ids) {
                delIds.add(Long.parseLong(id));
            }
        }
        contractSecondCycleService.deleteBatch(delIds);
        return success();
    }


    /**
     * Query option accounts
     * @param screen
     * @return
     */
    @RequiresPermissions("second-account:page-query")
    @PostMapping("account/page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Second contract account list")
    public MessageResult queryAccounts(ContractFinanceScreen screen) {
        Page<MemberSecondWallet> all = memberSecondWalletService.findAll(screen);
        return success(IPage2Page(all));
    }

}

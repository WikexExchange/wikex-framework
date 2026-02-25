package com.wikex.wikex.p2p.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.constant.DepositStatusEnum;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.p2p.entity.BusinessAuthApply;
import com.wikex.wikex.p2p.entity.BusinessAuthDeposit;
import com.wikex.wikex.p2p.entity.BusinessCancelApply;
import com.wikex.wikex.p2p.entity.DepositRecord;
import com.wikex.wikex.p2p.service.*;
import com.wikex.wikex.screen.ApplyScreen;
import com.wikex.wikex.screen.CancelApplyScreen;
import com.wikex.wikex.screen.DepositScreen;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("businessAuthFeign")
@Slf4j
public class BusinessAuthFeignController extends BaseController {
    @Autowired
    private BusinessAuthDepositService businessAuthDepositService;
    @Autowired
    private BusinessAuthApplyService businessAuthApplyService;
    @Autowired
    private BusinessCancelApplyService businessCancelApplyService;
    @Autowired
    private DepositRecordService depositRecordService;
    @Autowired
    private AdvertiseService advertiseService;


    @RequestMapping("findAllDeposit")
    public Page<BusinessAuthDeposit> findAllDeposit(@RequestBody DepositScreen screen){
        return businessAuthDepositService.findAll(screen.getPageNo(),screen.getPageSize(),screen.getStatus());
    }

    @RequestMapping("add")
    public MessageResult add(@RequestBody BusinessAuthDeposit businessAuthDeposit){
        businessAuthDepositService.save(businessAuthDeposit);
        return success();
    }

    @RequestMapping("detail")
    public MessageResult detail(@RequestParam("id")Long id){
        return businessAuthApplyService.detail(id);
    }

    @RequestMapping("findDepositById")
    public BusinessAuthDeposit findDepositById(@RequestParam("id")Long id){
        return businessAuthDepositService.getById(id);
    }

    @RequestMapping("updateDeposit")
    public MessageResult updateDeposit(@RequestBody BusinessAuthDeposit deposit){
        businessAuthDepositService.updateById(deposit);
        return success();
    }

    @RequestMapping("pageApply")
    public Page<BusinessAuthApply> pageApply(@RequestBody ApplyScreen screen){
        return businessAuthApplyService.pageApply(screen.getPageNo(),screen.getPageSize(),screen.getStatus(),screen.getAccount());
    }

    @PostMapping("findAllCancelApply")
    public Page<BusinessCancelApply> findAllCancelApply(@RequestBody CancelApplyScreen screen){
        return businessCancelApplyService.findAllCancelApply(screen.getPageNo(),screen.getPageSize(),screen.getStatus(),screen.getAccount(),screen.getStartDate(),screen.getEndDate());
    }

    @RequestMapping("findCancelApplyById")
    public BusinessCancelApply findCancelApplyById(@RequestParam("id")Long id){
        return businessCancelApplyService.getById(id);
    }

    @RequestMapping("findByMemberAndCertifiedBusinessStatus")
    public List<BusinessAuthApply> findByMemberAndCertifiedBusinessStatus(@RequestParam("memberId")Long memberId, @RequestParam("status")CertifiedBusinessStatus status){
        return businessAuthApplyService.findByMemberAndCertifiedBusinessStatus(memberId,status);
    }

    @RequestMapping("updateCancelApply")
    public MessageResult updateCancelApply(@RequestBody BusinessCancelApply businessCancelApply){
        businessCancelApplyService.updateById(businessCancelApply);
        return success();
    }

    @RequestMapping("findDepositByMemberAndStatus")
    public List<DepositRecord> findDepositByMemberAndStatus(@RequestParam("memberId") Long memberId, @RequestParam("status") DepositStatusEnum status){
        return depositRecordService.findDepositByMemberAndStatus(memberId,status);
    }

    @RequestMapping("updateDepositRecord")
    public MessageResult updateDepositRecord(@RequestBody DepositRecord depositRecord){
        depositRecordService.updateById(depositRecord);
        return success();
    }

    @RequestMapping("saveDepositRecord")
    public MessageResult saveDepositRecord(@RequestBody DepositRecord depositRecord){
        depositRecordService.saveOrUpdate(depositRecord);
        return success();
    }

    @RequestMapping("findDepositRecordById")
    public DepositRecord findDepositRecordById(@RequestParam("id")String id){
        return depositRecordService.getById(id);
    }

    @RequestMapping("getBusinessOrderStatistics")
    public Map<String, Object> getBusinessOrderStatistics(@RequestParam("memberId")Long memberId){
        return businessCancelApplyService.getBusinessOrderStatistics(memberId);
    }
    @RequestMapping("getBusinessAppealStatistics")
    public Map<String, Object> getBusinessAppealStatistics(@RequestParam("memberId")Long memberId){
        return businessCancelApplyService.getBusinessAppealStatistics(memberId);
    }

    @RequestMapping("getAdvertiserNum")
    public Long getAdvertiserNum(@RequestParam("memberId")Long memberId){
        return advertiseService.getAdvertiserNum(memberId);
    }

    @RequestMapping("applyCountAuditing")
    public Integer applyCountAuditing(){
        return businessAuthApplyService.countAuditing();
    }

    @RequestMapping("cancelCountAuditing")
    public Integer cancelCountAuditing(){
        return businessCancelApplyService.countAuditing();
    }
    @RequestMapping("saveBusinessAuthApply")
    public MessageResult saveBusinessAuthApply(@RequestBody BusinessAuthApply businessAuthApply){
        businessAuthApplyService.saveOrUpdate(businessAuthApply);
        return success();
    }
}

package com.wikex.wikex.p2p.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.constant.DepositStatusEnum;
import com.wikex.wikex.p2p.entity.BusinessAuthApply;
import com.wikex.wikex.p2p.entity.BusinessAuthDeposit;
import com.wikex.wikex.p2p.entity.BusinessCancelApply;
import com.wikex.wikex.p2p.entity.DepositRecord;
import com.wikex.wikex.screen.ApplyScreen;
import com.wikex.wikex.screen.CancelApplyScreen;
import com.wikex.wikex.screen.DepositScreen;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-p2p",contextId = "businessAuthFeign")
public interface BusinessAuthFeign {

    @RequestMapping("/businessAuthFeign/findAllDeposit")
    Page<BusinessAuthDeposit> findAllDeposit(@RequestBody DepositScreen screen);

    @RequestMapping("/businessAuthFeign/add")
    MessageResult add(@RequestBody BusinessAuthDeposit businessAuthDeposit);

    @RequestMapping("/businessAuthFeign/detail")
    MessageResult detail(@RequestParam("id")Long id);

    @RequestMapping("/businessAuthFeign/findDepositById")
    BusinessAuthDeposit findDepositById(@RequestParam("id")Long id);

    @RequestMapping("/businessAuthFeign/updateDeposit")
    MessageResult updateDeposit(@RequestBody BusinessAuthDeposit deposit);

    @RequestMapping("/businessAuthFeign/saveDepositRecord")
    MessageResult saveDepositRecord(@RequestBody DepositRecord depositRecord);

    @RequestMapping("/businessAuthFeign/pageApply")
    Page<BusinessAuthApply> pageApply(@RequestBody ApplyScreen screen);

    @PostMapping("/businessAuthFeign/findAllCancelApply")
    Page<BusinessCancelApply> findAllCancelApply(@RequestBody CancelApplyScreen screen);

    @RequestMapping("/businessAuthFeign/findCancelApplyById")
    BusinessCancelApply findCancelApplyById(@RequestParam("id")Long id);

    @RequestMapping("/businessAuthFeign/findByMemberAndCertifiedBusinessStatus")
    List<BusinessAuthApply> findByMemberAndCertifiedBusinessStatus(@RequestParam("memberId")Long memberId, @RequestParam("status")CertifiedBusinessStatus status);

    @RequestMapping("/businessAuthFeign/updateCancelApply")
    MessageResult updateCancelApply(@RequestBody BusinessCancelApply businessCancelApply);

    @RequestMapping("/businessAuthFeign/findDepositByMemberAndStatus")
    List<DepositRecord> findDepositByMemberAndStatus(@RequestParam("memberId") Long memberId, @RequestParam("status") DepositStatusEnum status);

    @RequestMapping("/businessAuthFeign/updateDepositRecord")
    MessageResult updateDepositRecord(@RequestBody DepositRecord depositRecord);

    @RequestMapping("/businessAuthFeign/findDepositRecordById")
    DepositRecord findDepositRecordById(@RequestParam("id")String id);

    @RequestMapping("/businessAuthFeign/getBusinessOrderStatistics")
    Map<String, Object> getBusinessOrderStatistics(@RequestParam("memberId")Long memberId);

    @RequestMapping("/businessAuthFeign/getBusinessAppealStatistics")
    Map<String, Object> getBusinessAppealStatistics(@RequestParam("memberId")Long memberId);

    @RequestMapping("/businessAuthFeign/getAdvertiserNum")
    Long getAdvertiserNum(@RequestParam("memberId")Long memberId);

    @RequestMapping("/businessAuthFeign/applyCountAuditing")
    Integer applyCountAuditing();

    @RequestMapping("/businessAuthFeign/cancelCountAuditing")
    Integer cancelCountAuditing();

    @RequestMapping("/businessAuthFeign/saveBusinessAuthApply")
    MessageResult saveBusinessAuthApply(@RequestBody BusinessAuthApply businessAuthApply);
}

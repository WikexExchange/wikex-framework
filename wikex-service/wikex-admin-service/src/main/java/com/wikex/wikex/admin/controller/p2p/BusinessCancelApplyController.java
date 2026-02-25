package com.wikex.wikex.admin.controller.p2p;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.constant.DepositStatusEnum;
import com.wikex.wikex.constant.MemberLevelEnum;
import com.wikex.wikex.p2p.entity.*;
import com.wikex.wikex.p2p.feign.BusinessAuthFeign;
import com.wikex.wikex.screen.CancelApplyScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;


@RestController
@RequestMapping("business/cancel-apply")
public class BusinessCancelApplyController extends BaseAdminController {
    private static Logger logger = LoggerFactory.getLogger(BusinessCancelApplyController.class);
    @Autowired
    private BusinessAuthFeign businessAuthFeign;
    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private MemberFeign memberService;
    @Autowired
    private LocaleMessageSourceService msService;
    @Autowired
    private CoinFeign coinFeign;

    @PostMapping("page-query")
    @RequiresPermissions("business:cancel-apply:page-query")
    public MessageResult pageQuery(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "account", required = false) String account,
            @RequestParam(value = "status", required = false) CertifiedBusinessStatus status,
            @RequestParam(value = "startDate", required = false)@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false)@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd") Date endDate) {
        CancelApplyScreen screen = new CancelApplyScreen();
        screen.setPageNo(pageNo);
        screen.setPageSize(pageSize);
        screen.setAccount(account);
        screen.setStatus(status);
        screen.setStartDate(startDate);
        screen.setEndDate(endDate);
        Page<BusinessCancelApply> page = businessAuthFeign.findAllCancelApply(screen);
        return success(IPage2Page(page));
    }

    /**
     * Insurance refund review API
     *
     * @param id
     * @param success   Pass: IS_TRUE
     * @param reason    Reason for rejection
     * @return
     */
    @RequiresPermissions("business:cancel-apply:check")
    @PostMapping("check")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult pass(
            @RequestParam(value = "id") Long id,
            @RequestParam(value = "success") BooleanEnum success,
            @RequestParam(value = "reason", defaultValue = "") String reason) {
        BusinessCancelApply businessCancelApply = businessAuthFeign.findCancelApplyById(id);
        Member member = memberService.findMemberById(businessCancelApply.getMemberId());
        List<BusinessAuthApply> businessAuthApplyList = businessAuthFeign.findByMemberAndCertifiedBusinessStatus(member.getId(), CertifiedBusinessStatus.VERIFIED);
        if (businessAuthApplyList == null || businessAuthApplyList.size() < 1) {
            return error("data exception, businessAuthApply not exist...");
        }
        BusinessAuthApply businessAuthApply = businessAuthApplyList.get(0);
        /**
         * Handle cancel application log
         */
        businessCancelApply.setHandleTime(DateUtil.getCurrentDate());
        businessCancelApply.setDepositRecordId(businessAuthApply.getDepositRecordId());
        businessCancelApply.setDetail(reason);

        if (success == BooleanEnum.IS_TRUE) {

            businessCancelApply.setStatus(CertifiedBusinessStatus.RETURN_SUCCESS.getCode());
            businessAuthFeign.updateCancelApply(businessCancelApply);

            // Cancel merchant certification - review passed
            member.setCertifiedBusinessStatus(CertifiedBusinessStatus.NOT_CERTIFIED);
            member.setMemberLevel(MemberLevelEnum.REALNAME.getCode());
            memberService.save(member);

            List<DepositRecord> depositRecordList = businessAuthFeign.findDepositByMemberAndStatus(member.getId(), DepositStatusEnum.PAY);
            if (depositRecordList != null && depositRecordList.size() > 0) {
                BigDecimal deposit = BigDecimal.ZERO;

                /**
                 * Update deposit record
                 */
                for (DepositRecord depositRecord : depositRecordList) {
                    depositRecord.setStatus(DepositStatusEnum.GET_BACK);
                    deposit = deposit.add(depositRecord.getAmount());
                    businessAuthFeign.updateDepositRecord(depositRecord);
                }

                /**
                 * Refund deposit
                 */
                if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                    BusinessAuthDeposit businessAuthDeposit =  businessAuthFeign.findDepositById(businessAuthApply.getBusinessAuthDepositId());
                    MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(businessAuthDeposit.getCoinId(), member.getId());
                    memberWallet.setBalance(memberWallet.getBalance().add(deposit));
                    // memberWallet.setFrozenBalance(memberWallet.getFrozenBalance().subtract(deposit));
                    memberWalletService.save(memberWallet);
                }
            }
            /**
             * Update certification application status
             */
            return MessageResult.success(msService.getMessage("PASS_THE_AUDIT"), reason);
        } else {
            // Review not passed, merchant remains verified
            member.setCertifiedBusinessStatus(CertifiedBusinessStatus.VERIFIED);
            member.setMemberLevel(MemberLevelEnum.IDENTIFICATION.getCode());
            memberService.save(member);

            businessCancelApply.setStatus(CertifiedBusinessStatus.RETURN_FAILED.getCode());
            businessAuthFeign.updateCancelApply(businessCancelApply);

            return MessageResult.success(msService.getMessage("AUDIT_DOES_NOT_PASS"), reason);
        }
    }

    /**
     * @param id: businessCancelApply id
     * @return
     */
    @PostMapping("detail")
    @RequiresPermissions("business:cancel-apply:detail")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        BusinessCancelApply businessCancelApply = businessAuthFeign.findCancelApplyById(id);
        if(businessCancelApply.getMemberId()!=null){
            businessCancelApply.setMember(memberService.findMemberById(businessCancelApply.getMemberId()));
        }
        DepositRecord depositRecord = businessAuthFeign.findDepositRecordById(businessCancelApply.getDepositRecordId());
        Map<String, Object> map1 = businessAuthFeign.getBusinessOrderStatistics(businessCancelApply.getMemberId());
        logger.info("Member order information: {}", map1);
        Map<String, Object> map2 = businessAuthFeign.getBusinessAppealStatistics(businessCancelApply.getMemberId());
        logger.info("Member appeal information: {}", map2);
        Long advertiseNum = businessAuthFeign.getAdvertiserNum(businessCancelApply.getMemberId());
        logger.info("Member advertisement information: {}", advertiseNum);
        Map<String, Object> map = new HashMap<>();
        map.putAll(map1);
        map.putAll(map2);
        map.put("advertiseNum", advertiseNum);
        map.put("businessCancelApply", businessCancelApply);
        map.put("depositRecord", depositRecord);
        logger.info("Member insurance refund related info: {}", map);
        return success(map);
    }

    @PostMapping("get-search-status")
    public MessageResult getSearchStatus() {
        CertifiedBusinessStatus[] statuses = CertifiedBusinessStatus.values();
        List<Map> list = new ArrayList<>();
        for (CertifiedBusinessStatus status : statuses) {
            if (status.getCode() < CertifiedBusinessStatus.CANCEL_AUTH.getCode()) {
                continue;
            }
            Map map = new HashMap();
            map.put("name", status.getDescription());
            map.put("value", status.getCode());
            list.add(map);
        }
        return success(list);
    }
}

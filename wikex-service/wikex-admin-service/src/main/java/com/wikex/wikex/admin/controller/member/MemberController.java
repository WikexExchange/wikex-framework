package com.wikex.wikex.admin.controller.member;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.p2p.entity.BusinessAuthApply;
import com.wikex.wikex.p2p.entity.BusinessAuthDeposit;
import com.wikex.wikex.p2p.entity.DepositRecord;
import com.wikex.wikex.p2p.feign.BusinessAuthFeign;
import com.wikex.wikex.screen.MemberScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.dto.MemberDTO;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.wikex.wikex.constant.CertifiedBusinessStatus.*;
import static com.wikex.wikex.constant.MemberLevelEnum.IDENTIFICATION;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;


@RestController
@RequestMapping("/member")
@Slf4j
public class MemberController extends BaseAdminController {

    @Autowired
    private MemberFeign memberService;
    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private BusinessAuthFeign businessAuthFeign;
    @Autowired
    private CoinFeign coinFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("member:page-query")
    @PostMapping("page-query")
    @ResponseBody
    @AccessLog(module = AdminModule.MEMBER, operation = "Paginated query of members")
    public MessageResult page(MemberScreen screen) {
        Page<Member> all = memberService.findAll(screen,screen.getPageNo(),screen.getPageSize());

        return success(IPage2Page(all));
    }

    @RequiresPermissions("member:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.MEMBER, operation = "Member details")
    public MessageResult detail(@RequestParam("id") Long id) {
        Member member = memberService.findMemberById(id);
        notNull(member, "validate id!");
        List<MemberWallet> list = memberWalletService.findAllByMemberId(member.getId());
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMember(member);
        memberDTO.setList(list);
        return success(memberDTO);
    }

    @RequiresPermissions("member:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.MEMBER, operation = "All members")
    public MessageResult all() {
        List<Member> all = memberService.findAllList();
        if (all != null && all.size() > 0) {
            return success(all);
        }
        return error(messageSource.getMessage("REQUEST_FAILED"));
    }

    @RequiresPermissions("member:delete")
    @PostMapping("delete")
    @AccessLog(module = AdminModule.MEMBER, operation = "Delete member")
    public MessageResult delete(@RequestParam(value = "id") Long id) {
        Member member = memberService.findMemberById(id);
        notNull(member, "validate id!");
        member.setStatus(CommonStatus.ILLEGAL.getCode());// Set status to illegal
        memberService.updateMemberById(member);
        return success();
    }

    @RequiresPermissions("member:update")
    @PostMapping(value = "update")
    @AccessLog(module = AdminModule.MEMBER, operation = "Update member")
    public MessageResult update(Member member) {
        if (member.getId() == null) {
            return error(messageSource.getMessage("ID_REQUIRED"));
        }
        Member one = memberService.findMemberById(member.getId());
        if (one == null) {
            return error(messageSource.getMessage("USER_NOT_FOUND"));
        }
        if (StringUtils.isNotBlank(member.getUsername())) {
            one.setUsername(member.getUsername());
        }
        if (StringUtils.isNotBlank(member.getPassword())) {
            one.setPassword(member.getPassword());
        }
        if (StringUtils.isNotBlank(member.getRealName())) {
            one.setRealName(member.getRealName());
        }
        memberService.updateMemberById(one);
        return success(one);
    }

    @RequiresPermissions("member:audit-business")
    @PatchMapping("{id}/audit-business")
    @AccessLog(module = AdminModule.MEMBER, operation = "Member business certification")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult auditBusiness(
            @PathVariable("id") Long id,
            @RequestParam("status") CertifiedBusinessStatus status,
            @RequestParam("detail") String detail) {
        Member member = memberService.findMemberById(id);
        notNull(member, "validate id!");
        // Confirm status is under review
        isTrue(member.getCertifiedBusinessStatus() == AUDITING, "validate member certifiedBusinessStatus!");
        // Ensure the incoming certifiedBusinessStatus is correct, either approved or failed
        isTrue(status == VERIFIED || status == FAILED, "validate certifiedBusinessStatus!");
        List<BusinessAuthApply> businessAuthApplyList = businessAuthFeign.findByMemberAndCertifiedBusinessStatus(member.getId(), AUDITING);
        if (status == VERIFIED) {
            // Approved
            member.setCertifiedBusinessStatus(VERIFIED); // Verified
            member.setMemberLevel(IDENTIFICATION.getCode()); // Certified business member
            if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                BusinessAuthApply businessAuthApply = businessAuthApplyList.get(0);
                businessAuthApply.setCertifiedBusinessStatus(VERIFIED);
                // If a deposit policy was chosen during application
                if (businessAuthApply.getBusinessAuthDepositId() != null) {
                    BusinessAuthDeposit deposit = businessAuthFeign.findDepositById(businessAuthApply.getBusinessAuthDepositId());
                    deposit.setCoin(coinFeign.findByCoinId(deposit.getCoinId()));
                    // Deduct deposit
                    MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(deposit.getCoin().getUnit(), member.getId());
                    memberWalletService.decreaseFrozen(memberWallet.getId(),businessAuthApply.getAmount());
                    DepositRecord depositRecord = new DepositRecord();
                    depositRecord.setId(UUID.randomUUID().toString());
                    depositRecord.setAmount(businessAuthApply.getAmount());
                    depositRecord.setCoinId(deposit.getCoinId());
                    depositRecord.setMemberId(member.getId());
                    depositRecord.setStatus(DepositStatusEnum.PAY);
                    businessAuthFeign.saveDepositRecord(depositRecord);
                    businessAuthApply.setDepositRecordId(depositRecord.getId());
                }
                businessAuthFeign.saveBusinessAuthApply(businessAuthApply);
            }
        } else {
            // Rejected
            member.setCertifiedBusinessStatus(FAILED); // Verification failed
            if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
                BusinessAuthApply businessAuthApply = businessAuthApplyList.get(0);
                businessAuthApply.setCertifiedBusinessStatus(FAILED);
                businessAuthApply.setDetail(detail);
                // Refund the frozen deposit from the application
                if (businessAuthApply.getBusinessAuthDepositId() != null) {
                    BusinessAuthDeposit deposit = businessAuthFeign.findDepositById(businessAuthApply.getBusinessAuthDepositId());
                    memberWalletService.thawBalance(deposit.getCoinId(),member.getId(),businessAuthApply.getAmount());
                }
                businessAuthFeign.saveBusinessAuthApply(businessAuthApply);
            }
        }
        member.setCertifiedBusinessCheckTime(new Date());
        memberService.save(member);
        return success();
    }

    @RequiresPermissions("member:audit-business")
    @GetMapping("{id}/business-auth-detail")
    @AccessLog(module = AdminModule.MEMBER, operation = "Query member application information")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult getBusinessAuthApply(@PathVariable("id") Long id,
                                              @RequestParam("status") CertifiedBusinessStatus status) {
        if (status == null) {
            return MessageResult.error("Missing parameter");
        }
        isTrue(status == AUDITING || status == CANCEL_AUTH, "validate certifiedBusinessStatus!");
        Member member = memberService.findMemberById(id);
        notNull(member, "validate id!");
        // Query application records
        List<BusinessAuthApply> businessAuthApplyList = businessAuthFeign.findByMemberAndCertifiedBusinessStatus(member.getId(), status);
        MessageResult result = MessageResult.success();
        if (businessAuthApplyList != null && businessAuthApplyList.size() > 0) {
            result.setData(businessAuthApplyList.get(0));
        }
        return result;
    }

    @RequiresPermissions("member:alter-publish-advertisement-status")
    @PostMapping("alter-publish-advertisement-status")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Disable/Enable advertisement publishing")
    public MessageResult publishAdvertise(@RequestParam("memberId") Long memberId,
                                          @RequestParam("status") BooleanEnum status) {
        Member member = memberService.findMemberById(memberId);
        if (member.getCertifiedBusinessStatus() != CertifiedBusinessStatus.VERIFIED) {
            return error(messageSource.getMessage("PLEASE_CERTIFY_AS_MERCHANT"));
        }
        Assert.notNull(member, messageSource.getMessage("PLAYER_NOT_FOUND"));
        member.setPublishAdvertise(status.getCode());
        memberService.save(member);
        return success(status == BooleanEnum.IS_FALSE ? messageSource.getMessage("PROHIBIT_ADVERTISEMENT_PUBLICATION_SUCCESS") : messageSource.getMessage("PROHIBIT_REMOVAL_SUCCESS"));
    }

    @RequiresPermissions("member:alter-status")
    @PostMapping("alter-status")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Disable/Enable member account")
    public MessageResult ban(@RequestParam("status") CommonStatus status,
                             @RequestParam("memberId") Long memberId) {
        Member member = memberService.findMemberById(memberId);
        member.setStatus(status.getCode());
        memberService.save(member);
        
        return success(messageSource.getMessage("SUCCESS"));
    }

    @RequiresPermissions("member:alter-transaction-status")
    @PostMapping("alter-transaction-status")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Disable/Enable member account transaction")
    public MessageResult alterTransactionStatus(
            @RequestParam("status") BooleanEnum status,
            @RequestParam("memberId") Long memberId) {
        Member member = memberService.findMemberById(memberId);
        member.setTransactionStatus(status.getCode());
        memberService.save(member);
        return success(messageSource.getMessage("SUCCESS"));
    }

    /**
     * Change user level (partner/agent etc.)
     * @param superPartner
     * @param memberId
     * @return
     */
    @RequiresPermissions("member:alter-member-superpartner")
    @PostMapping("alter-member-superpartner")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Modify user level")
    public MessageResult alterSuperPartner(
            @RequestParam("superPartner") String superPartner,
            @RequestParam("memberId") Long memberId) {
        Member member = memberService.findMemberById(memberId);
        member.setSuperPartner(superPartner);
        memberService.save(member);
        return success(messageSource.getMessage("SUCCESS"));
    }

    /**
     * Query agent list
     */
    @RequiresPermissions("member:page-query-super")
    @PostMapping("page-query-super")
    @ResponseBody
    @AccessLog(module = AdminModule.MEMBER, operation = "Paginated query of members")
    public MessageResult pageSuperPartner(MemberScreen screen) {
        screen.setSuperPartner("1"); // Default select agents
        Page<Member> all = memberService.findAll(screen,screen.getPageNo(),screen.getPageSize());
        return success(IPage2Page(all));
    }

    /**
     * Query agent invited user list
     */
    @RequiresPermissions("member:supermember-page-query")
    @PostMapping(value = "/supermember-page-query")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public MessageResult pageSuperMember(
            MemberScreen screen,
            Long userId) {
        // Check if the user is an agent
        Member checkMember = memberService.findMemberById(userId);
        if(!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }
        screen.setInviterId(userId);
        Page<Member> all = memberService.findAll(screen, screen.getPageNo(),screen.getPageSize());
        return success(IPage2Page(all));
    }

    @RequiresPermissions("member:set-inviter")
    @PostMapping("setInviter")
    @AccessLog(module = AdminModule.MEMBER, operation = "Set inviter")
    public MessageResult setInviter(
            @RequestParam(value = "id") Long id,
            @RequestParam(value = "inviterId") Long inviterId) throws Exception {
        memberService.setMemberInviter(id,inviterId);
        return success();
    }
}

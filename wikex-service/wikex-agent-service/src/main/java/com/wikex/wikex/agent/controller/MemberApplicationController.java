package com.wikex.wikex.agent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.MemberApplicationScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberApplication;
import com.wikex.wikex.user.feign.MemberApplicationFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.MemberApplicationVo;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.util.Assert.notNull;

import java.util.List;

/**
 * @author Hevin
 * @description Real-name authentication application
 * @date 2019/12/26 15:05
 */
@RestController
@RequestMapping("member/member-application")
public class MemberApplicationController extends BaseController {
    @Autowired
    private SMSProvider smsProvider;
    @Autowired
    private MemberApplicationFeign memberApplicationFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private MemberFeign memberFeign;

    @PostMapping("all")
    public MessageResult all() {
        List<MemberApplication> all = memberApplicationFeign.fetch();
        if (all != null && all.size() > 0) {
            return success(all);
        }
        return error(messageSource.getMessage("NO_DATA"));
    }

    @PostMapping("detail")
    public MessageResult detail(@RequestParam("id") Long id) {
        MemberApplication memberApplication = memberApplicationFeign.findById(id);
        notNull(memberApplication, "validate id!");
        return success(memberApplication);
    }

    /**
     * Paginated query for member authentication applications
     *
     * @param screen
     * @param authMember
     * @return
     */
    @PostMapping("page-query")
    @PermissionOperation
    public MessageResult queryPage(MemberApplicationScreen screen,
                                   @RequestHeader(SysConstant.SESSION_MEMBER) String authMember
    ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member checkMember = memberFeign.findMemberById(user.getId());
        if (!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }
        screen.setInviterId(checkMember.getId());
        Page<MemberApplicationVo> all = memberApplicationFeign.findAll(screen);
        return success(IPage2Page(all));
    }

    /**
     * Approve authentication application
     *
     * @param id application ID
     * @return
     */
    @PatchMapping("{id}/pass")
    public MessageResult pass(@PathVariable("id") Long id) {
        // Validate
        MemberApplication application = memberApplicationFeign.findById(id);
        notNull(application, "validate id!");
        // Business logic
        memberApplicationFeign.auditPass(application);
        // Send notification
        try {
            Member memberById = memberFeign.findMemberById(application.getMemberId());
            smsProvider.sendCustomMessage(memberById.getMobilePhone(), messageSource.getMessage("CONGRATULATIONS_AUTH_APPROVED"));
        } catch (Exception e) {
            return error(e.getMessage());
        }
        // Return
        return success();
    }

    /**
     * Reject authentication application
     *
     * @param id application ID
     * @param rejectReason reason for rejection
     * @return
     */
    @PatchMapping("{id}/no-pass")
    public MessageResult noPass(
            @PathVariable("id") Long id,
            @RequestParam(value = "rejectReason", required = false) String rejectReason) {
        // Validate
        MemberApplication application = memberApplicationFeign.findById(id);
        notNull(application, "validate id!");
        // Business logic
        // application.setRejectReason(rejectReason); // rejection reason
        memberApplicationFeign.auditNotPass(application);

        try {
            Member memberById = memberFeign.findMemberById(application.getMemberId());
            smsProvider.sendCustomMessage(memberById.getMobilePhone(), messageSource.getMessage("AUTH_REJECTED_PLEASE_REAPPLY"));
        } catch (Exception e) {
            return error(e.getMessage());
        }
        // Return
        return success();
    }

    private void sendMsg() {
        // Reserved for future message sending
    }
}

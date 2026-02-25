package com.wikex.wikex.admin.controller.member;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.MemberApplicationScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberApplication;
import com.wikex.wikex.user.feign.MemberApplicationFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.vo.MemberApplicationVo;
import com.wikex.wikex.util.MessageResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.util.Assert.notNull;


@RestController
@RequestMapping("member/member-application")
public class MemberApplicationController extends BaseAdminController {
	@Autowired
    private SMSProvider smsProvider;
    @Autowired
    private MemberApplicationFeign memberApplicationFeign;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("member:member-application:all")
    @PostMapping("all")
    @AccessLog(module = AdminModule.MEMBER, operation = "All member MemberApplication verification information")
    public MessageResult all(MemberApplicationScreen screen) {
        Page<MemberApplicationVo> all = memberApplicationFeign.findAll(screen);
        return success(IPage2Page(all));

    }

    @RequiresPermissions("member:member-application:detail")
    @PostMapping("detail")
    @AccessLog(module = AdminModule.MEMBER, operation = "Member MemberApplication verification information detail")
    public MessageResult detail(@RequestParam("id") Long id) {
        MemberApplication memberApplication = memberApplicationFeign.findById(id);
        notNull(memberApplication, "validate id!");
        return success(memberApplication);
    }

    @RequiresPermissions("member:member-application:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.MEMBER, operation = "Paginated query of member MemberApplication verification information")
    public MessageResult queryPage(MemberApplicationScreen screen) {
        Page<MemberApplicationVo> all = memberApplicationFeign.findAll(screen);
        return success(IPage2Page(all));
    }

    @RequiresPermissions("member:member-application:pass")
    @PostMapping("pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "Member MemberApplication verification approved")
    public MessageResult pass(@RequestParam("id") Long id) {
        // Validation
        MemberApplication application = memberApplicationFeign.findById(id);
        notNull(application, "validate id!");
        // Business logic
        memberApplicationFeign.auditPass(application);
        // Send notification
        try {
            Member member = memberFeign.findMemberById(application.getMemberId());
            smsProvider.sendCustomMessage(member.getMobilePhone(), messageSource.getMessage("CONGRATULATIONS_AUTH_APPROVED"));
		} catch (Exception e) {
			return error(e.getMessage());
		}
        // Return
        return success();
    }

    @RequiresPermissions("member:member-application:no-pass")
    @PostMapping("no-pass")
    @AccessLog(module = AdminModule.MEMBER, operation = "Member MemberApplication verification not approved")
    public MessageResult noPass(
            @RequestParam("id") Long id,
            @RequestParam(value = "rejectReason", required = false) String rejectReason) {
        // Validation
        MemberApplication application = memberApplicationFeign.findById(id);
        notNull(application, "validate id!");
        // Business logic
        application.setRejectReason(rejectReason); // Reason for rejection
        memberApplicationFeign.auditNotPass(application);

        try {
            Member member = memberFeign.findMemberById(application.getMemberId());
			smsProvider.sendCustomMessage(member.getMobilePhone(), messageSource.getMessage("AUTH_REJECTED_PLEASE_REAPPLY"));
		} catch (Exception e) {
			return error(e.getMessage());
		}
        // Return
        return success();
    }
}

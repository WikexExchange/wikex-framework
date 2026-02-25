package com.wikex.wikex.admin.controller;

import com.wikex.wikex.admin.entity.Feedback;
import com.wikex.wikex.admin.service.FeedbackService;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class FeedbackController extends BaseController {
    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private LocaleMessageSourceService msService;

    /**
     *
     * @param
     * @param remark
     * @return
     */
    @PermissionOperation
    @RequestMapping("feedback")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult feedback(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, String remark) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Feedback feedback = new Feedback();
        feedback.setMemberId(user.getId());
        feedback.setRemark(remark);
        boolean save = feedbackService.save(feedback);
        if (save) {
            return MessageResult.success();
        } else {
            return MessageResult.error(msService.getMessage("SYSTEM_ERROR"));
        }
    }
}

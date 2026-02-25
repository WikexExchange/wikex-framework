package com.wikex.wikex.agent.controller;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.MemberDepositScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberDeposit;
import com.wikex.wikex.user.feign.MemberDepositFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("deposit")
@Slf4j
public class MemberDepositRecordController extends BaseController {
    @Autowired
    private MemberDepositFeign memberDepositFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;
    @Autowired
    private MemberFeign memberFeign;
    /**
     * 
     *
     * @param screen
     * @return
     */

    @RequestMapping(value = "/page-query")
    @Transactional(rollbackFor = Exception.class)
    @PermissionOperation
    public MessageResult page(MemberDepositScreen screen, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member checkMember = memberFeign.findMemberById(user.getId());
        if(!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }
        screen.setInviterId(checkMember.getId());
        Page<MemberDeposit> page = memberDepositFeign.findAll(screen);
        return success(messageSource.getMessage("SUCCESS"), IPage2Page(page));
    }
}

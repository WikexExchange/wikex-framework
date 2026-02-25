package com.wikex.wikex.agent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.WithdrawScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.Withdraw;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.WithdrawFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static org.springframework.util.Assert.notNull;

@RestController
@RequestMapping("withdraw")
@Slf4j
public class WithdrawController extends BaseController {
    @Autowired
    private WithdrawFeign withdrawFeign;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private MemberFeign memberFeign;

    @PermissionOperation
    @RequestMapping(value = "/page-query")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult pageQuery(
            WithdrawScreen screen,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member checkMember = memberFeign.findMemberById(user.getId());
        if(!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }

        Page<Withdraw> pageListMapResult = withdrawFeign.joinFind(screen);
        return success(IPage2Page(pageListMapResult));
    }

    @GetMapping("/{id}")
    public MessageResult detail(@PathVariable("id") Long id) {
        Withdraw withdraw = withdrawFeign.findOne(id);
        notNull(withdraw, messageSource.getMessage("NO_DATA"));
        return success(withdraw);
    }
}

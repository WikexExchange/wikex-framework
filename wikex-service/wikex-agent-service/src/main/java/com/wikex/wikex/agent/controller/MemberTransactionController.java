package com.wikex.wikex.agent.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.MemberTransactionScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.user.vo.MemberTransactionVO;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import static org.springframework.util.Assert.notNull;

import javax.persistence.EntityManager;
import java.util.ArrayList;


@RestController
@RequestMapping("transactions")
@Slf4j
public class MemberTransactionController extends BaseController {
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequestMapping(value = "/detail")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        MemberTransaction memberTransaction = memberTransactionFeign.findOne(id);
        notNull(memberTransaction, "validate id!");
        return success(memberTransaction);
    }

    @RequestMapping(value = "/page-query")
    @Transactional(rollbackFor = Exception.class)
    @PermissionOperation
    public MessageResult pageQuery(
            MemberTransactionScreen screen,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member checkMember = memberFeign.findMemberById(user.getId());
        if(!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }
        Page<MemberTransactionVO> results = memberTransactionFeign.joinFind(screen);

        return success(IPage2Page(results));
    }
}

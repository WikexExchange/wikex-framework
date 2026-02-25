package com.wikex.wikex.agent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.entity.ContractRewardRecord;
import com.wikex.wikex.swap.feign.ContractRewardRecordFeign;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import static org.springframework.util.Assert.notNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sulinxin
 */
@RestController
@RequestMapping("transactionRebates")
@Slf4j
public class MemberTransactionRebateController extends BaseController {

    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ContractRewardRecordFeign contractRewardRecordFeign;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequestMapping(value = "/page-query")
    @Transactional(rollbackFor = Exception.class)
    @PermissionOperation
    public MessageResult pageQuery(
            ContractRewardRecordScreen screen,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member checkMember = memberFeign.findMemberById(user.getId());
        if(!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }

        if (screen.getDirection() == null && screen.getProperty() == null) {
            ArrayList<Sort.Direction> directions = new ArrayList<>();
            directions.add(Sort.Direction.DESC);
            screen.setDirection(directions);
            List<String> property = new ArrayList<>();
            property.add("createTime");
            screen.setProperty(property);
        }

        Page<ContractRewardRecord> results = contractRewardRecordFeign.findAll(screen);

        return success(IPage2Page(results));
    }
}

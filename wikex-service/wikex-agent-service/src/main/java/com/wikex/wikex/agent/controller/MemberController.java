package com.wikex.wikex.agent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.MemberScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.dto.MemberDTO;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.FileUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import static org.springframework.util.Assert.notNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

 // management (Admin backend)
@RestController
@RequestMapping("member")
@Slf4j
public class MemberController extends BaseController {

    @Autowired
    private MemberFeign memberFeign;

    @Autowired
    private MemberWalletFeign memberWalletFeign;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @PostMapping("all")
    public MessageResult all() {
        List<Member> all = memberFeign.findAllList();
        if (all != null && all.size() > 0) {
            return success(all);
        }
        return error(messageSource.getMessage("REQUEST_FAILED"));
    }

    @RequestMapping(value = "/detail")
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult detail(@RequestParam("id") Long id, @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member checkMember = memberFeign.findMemberById(user.getId());
        if (!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }

        Member member = memberFeign.findMemberById(id);
        notNull(member, "validate id!");
        List<MemberWallet> list = memberWalletFeign.findAllByMemberId(member.getId());
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMember(member);
        memberDTO.setList(list);
        return success(memberDTO);
    }

    /**
     * Paginated query for user list
     * @param screen filter conditions
     * @return user list
     */
    @RequestMapping(value = "/page-query")
    @Transactional(rollbackFor = Exception.class)
    @PermissionOperation
    public MessageResult page(
            MemberScreen screen,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        // Check if user is an agent
        Member checkMember = memberFeign.findMemberById(user.getId());
        if (!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }
        screen.setInviterId(checkMember.getId());
        Page<Member> all = memberFeign.findAll(screen, screen.getPageNo(), screen.getPageSize());
        return success(IPage2Page(all));
    }

    /**
     * Get asset list of a specific user
     * @param memberId member ID
     * @return asset list
     */
    @RequestMapping(value = "/assets-list")
    @Transactional(rollbackFor = Exception.class)
    @PermissionOperation
    public MessageResult getUserAssets(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            Long memberId) {
        List<MemberWallet> list = memberWalletFeign.findAllByMemberId(memberId);
        return success(messageSource.getMessage("SUCCESS"), list);
    }

    /**
     * Modify super partner level of a member
     */
    @RequestMapping(value = "/alter-superpartner")
    @PermissionOperation
    @Transactional(rollbackFor = Exception.class)
    public MessageResult alterSuperPartner(
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
            @RequestParam("superPartner") String superPartner,
            @RequestParam("memberId") Long memberId) {
        // Compare levels: users with lower levels cannot set higher levels for their subordinates
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member currentUser = memberFeign.findMemberById(user.getId());
        if (superPartner.compareTo(currentUser.getSuperPartner()) <= 0) {
            return error(messageSource.getMessage("CANNOT_SET_HIGHER_LEVEL_THAN_YOURSELF"));
        }
        Member member = memberFeign.findMemberById(memberId);
        member.setSuperPartner(superPartner);
        memberFeign.save(member);
        return success(messageSource.getMessage("SUCCESS"));
    }

    /**
     * Export member list to Excel
     */
    @GetMapping("out-excel")
    public MessageResult outExcel(
            MemberScreen screen,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        List list = memberFeign.findAllWithCondition(screen);
        return new FileUtil().exportExcel(request, response, list, "member");
    }
}

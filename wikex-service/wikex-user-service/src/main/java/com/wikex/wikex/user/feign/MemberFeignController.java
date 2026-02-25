package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.InviteManagementScreen;
import com.wikex.wikex.screen.MemberScreen;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.event.MemberEvent;
import com.wikex.wikex.user.service.MemberApplicationService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.Validate.notNull;

/*****
 * @Author:
 * @Description:
 ****/
@RestController
@RequestMapping("/memberFeign")
public class MemberFeignController extends BaseController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberEvent memberEvent;
    @Autowired
    private MemberApplicationService memberApplicationService;

    @GetMapping("findMemberById")
    public Member findMemberById(@RequestParam("id") Long id) {
        Member member = memberService.getById(id);
        return member;

    }

    @PostMapping("updateMemberById")
    public MessageResult updateMemberById(@RequestBody Member member) {
        boolean update = memberService.updateById(member);
        if (update) {
            return success();
        } else {
            return error("Update Failed");
        }
    }

    @PostMapping("findSuperPartnerMembersByIds")
    public MessageResult findSuperPartnerMembersByIds(@RequestParam("upper") String upper) {
        List<Member> list = memberService.findSuperPartnerMembersByIds(upper);
        return success(list);
    }

    @PostMapping("findAll")
    public Page<Member> findAll(
            @RequestBody MemberScreen screen,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize) {
        Page<Member> page = memberService.findAll(screen, pageNo, pageSize);
        return page;
    }

    @GetMapping("findAllList")
    public List<Member> findAllList() {
        List<Member> list = memberService.list();
        return list;
    }

    @GetMapping(value = "findAllWithCondition")
    public List<Member> findAllWithCondition(@RequestBody MemberScreen screen) {
        return memberService.findAllWithCondition(screen);
    }

    @RequestMapping(value = "look")
    public Page<Member> lookAll(@RequestBody InviteManagementScreen screen) {
        Page<Member> page = memberService.lookAll(screen);
        return page;
    }

    @RequestMapping(value = "queryFirstAndSecondById")
    public Page<Member> queryFirstAndSecondById(@RequestBody InviteManagementScreen screen) {
        Page<Member> page = memberService.queryFirstAndSecondById(screen);
        return page;
    }

    @RequestMapping(value = "findPromotionMember")
    public List<Member> findPromotionMember(@RequestParam("id") Long id) {
        List<Member> list = memberService.findPromotionMember(id);
        return list;
    }

    @RequestMapping(value = "findByPhone")
    public Member findByPhone(@RequestParam("phone") String phone) {
        return memberService.findByPhone(phone);
    }

    @RequestMapping(value = "findByEmail")
    public Member findByEmail(@RequestParam("email") String email) {
        return memberService.findByEmail(email);
    }

    @PostMapping(value = "save")
    MessageResult save(@RequestBody Member member) {
        memberService.updateById(member);
        return MessageResult.success();
    }

    @PostMapping(value = "findMemberIdsByAccount")
    public List<Long> findMemberIdsByAccount(@RequestParam("account") String account) {
        return memberService.findMemberIdsByAccount(account);
    }

    @PostMapping(value = "findMemberIdsByAccountAndNotCertified")
    public List<Long> findMemberIdsByAccountAndNotCertified(@RequestParam("account") String account) {
        return memberService.findMemberIdsByAccountAndNotCertified(account);
    }

    @PostMapping(value = "mapByMemberIds")
    public Map<Long, Member> mapByMemberIds(@RequestParam("ids") List<Long> ids) {
        if (ids != null && ids.size() > 0) {
            return memberService.mapByMemberIds(ids);
        } else {
            return new HashMap<>();
        }
    }

    @PostMapping(value = "findByUsername")
    public Member findByUsername(@RequestParam("name") String name) {
        return memberService.findByUsername(name);
    }

    @PostMapping(value = "login")
    public Member login(@RequestParam("name") String name, @RequestParam("password") String password) throws Exception {
        return memberService.login(name, password);
    }

    @PostMapping(value = "countAuditing")
    public Integer countAuditing() {
        return memberApplicationService.countAuditing();
    }

    @PostMapping(value = "getRegistrationNum")
    public int getRegistrationNum(@RequestParam("dateStr") String dateStr) {
        return this.memberService.getRegistrationNum(dateStr);
    }

    @PostMapping(value = "getBussinessNum")
    public int getBussinessNum(@RequestParam("dateStr") String dateStr) {
        return this.memberService.getBussinessNum(dateStr);
    }

    @PostMapping(value = "getApplicationNum")
    public int getApplicationNum(@RequestParam("dateStr") String dateStr) {
        return this.memberService.getApplicationNum(dateStr);
    }

    @PostMapping(value = "getStartRegistrationDate")
    public Date getStartRegistrationDate() {
        return this.memberService.getStartRegistrationDate();
    }

    @PostMapping(value = "setMemberInviter")
    MessageResult setMemberInviter(@RequestParam("id") Long id, @RequestParam("inviterId") Long inviterId)
            throws InterruptedException {
        Member member = memberService.getById(id);
        notNull(member, "validate id!");
        Member pMember = memberService.getById(inviterId);
        notNull(member, "validate id!");
        if (member.getInviterId() != null) {
            return error("Inviter already exists");
        }
        memberEvent.setMemberInviter(member, pMember);
        return success();

    }

}

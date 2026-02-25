package com.wikex.wikex.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.api.client.util.SecurityUtils;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberDeposit;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.transform.AuthMember;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import com.wikex.wikex.constant.SysConstant;
import static com.wikex.wikex.constant.SysConstant.SESSION_MEMBER;

@Api(tags = "Deposit Controller")
@RestController
@RequestMapping("/deposit")
@Slf4j
public class DepositController extends BaseController {
  @Autowired
  private MemberService memberService;
  @Autowired
  private MemberDepositService memberDepositService;

  @Autowired
  private LocaleMessageSourceService messageSourceService;

  @GetMapping("/list")
  public Page<MemberDeposit> listMyDeposits(
      @RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size

  ) {
    AuthMember user = AuthMember.toAuthMember(authMember);
    Assert.notNull(user, messageSourceService.getMessage("RE_LOGIN"));
    Member member = memberService.getById(user.getId());
    Assert.notNull(member, messageSourceService.getMessage("RE_LOGIN"));
    Page<MemberDeposit> pageSize = new Page<>(page, size);
    return memberDepositService.listDeposit(pageSize, member.getId().intValue());
  }

}
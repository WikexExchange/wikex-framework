package com.wikex.wikex.user.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.MySteryBox;
import com.wikex.wikex.user.service.MySteryBoxService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

import static com.wikex.wikex.constant.SysConstant.SESSION_MEMBER;

@Api(tags = "My Stery Box")
@RestController
@RequestMapping("/steryBox")
public class MySteryBoxController extends BaseController {

    @Autowired
    private MySteryBoxService mySteryBoxService;

    @Autowired
    private LocaleMessageSourceService msService;

    @ApiOperation(value = "Get code information")
    @GetMapping("findByCode")
    public MessageResult findByCode(@RequestParam("code") String code) {
        MySteryBox box = mySteryBoxService.findAllByCode(code);
        if (box == null) {
            return error(msService.getMessage("MYSTERY_BOX_NOT_FOUND"));
        }
        return success(box);
    }

    @ApiOperation(value = "Get mystery box information")
    @PermissionOperation
    @GetMapping("findByMemberId")
    public MessageResult findByMemberId(@RequestHeader(SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        MySteryBox box = mySteryBoxService.findAllByMemberId(member.getId());
        return success(box);
    }

    @ApiOperation(value = "Update mystery box information")
    @PermissionOperation
    @PostMapping(value = "/save")
    public MessageResult saveOrUpdate(@RequestHeader(SESSION_MEMBER) String authMember, String code) {
        if (code == null || code.trim().isEmpty()) {
            return error(msService.getMessage("CODE_REQUIRED"));
        }

        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        MySteryBox existingBox = mySteryBoxService.findAllByCode(code);
        if (existingBox == null) {
            return error(msService.getMessage("MYSTERY_BOX_NOT_FOUND"));
        }

        existingBox.setMember_id(memberId);
        existingBox.setMember_active_at(new Date(System.currentTimeMillis()));

        mySteryBoxService.saveOrUpdate(existingBox);
        return success();
    }
}

package com.wikex.wikex.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.Recharge;
import com.wikex.wikex.user.service.RechargeService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * Member User Frontend Controller
 * </p>
 *
 * @author markchao
 * @since 2021-06-14
 */
@Api(tags = "Recharge Record")
@RestController
@RequestMapping("/recharge")
public class RechargeController extends BaseController {

    @Autowired
    private RechargeService rechargeService;

    /**
     * Recharge Record List
     */
    @ApiOperation(value = "Recharge Record List")
    @PermissionOperation
    @PostMapping("list")
    public MessageResult list(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                              @RequestParam("page") Integer page,
                              @RequestParam("pageSize") Integer pageSize) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        MessageResult mr = new MessageResult(0, "success");

        Page<Recharge> records = rechargeService.findAllByMemberId(user.getId(), page, pageSize);
        mr.setData(IPage2Page(records));
        return mr;
    }

}

package com.wikex.wikex.admin.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.screen.MemberTransactionScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.vo.MemberTransactionVO;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequestMapping("/finance/member-transaction")
public class MemberTransactionController extends BaseAdminController {

    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private MemberTransactionFeign memberTransactionService;

    @RequiresPermissions(value = {
            "finance:member-transaction:page-query",
            "finance:member-transaction:page-query:recharge",
            "finance:member-transaction:page-query:check",
            "finance:member-transaction:page-query:fee"
    }, logical = Logical.OR)
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.FINANCE, operation = "Paginated query of trading records (MemberTransaction)")
    public MessageResult pageQuery(MemberTransactionScreen screen) {
        Page<MemberTransactionVO> results = memberTransactionService.joinFind(screen);
        return success(IPage2Page(results));
    }
}

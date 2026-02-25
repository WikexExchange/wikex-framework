package com.wikex.wikex.agent.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.ContractOrderEntrustScreen;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.wikex.wikex.swap.feign.ContractCoinFeign;
import com.wikex.wikex.swap.feign.ContractOrderEntrustFeign;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.wikex.wikex.constant.SysConstant.SESSION_MEMBER;


@RestController
@RequestMapping("/swap/order")
@Slf4j
public class ContractOrderEntrustController extends BaseController {
    @Autowired
    private ContractOrderEntrustFeign contractOrderEntrustFeign;

    @Autowired
    private MemberFeign memberFeign;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ContractCoinFeign contractCoinFeign;

    @Autowired
    private LocaleMessageSourceService messageSource;


    @PostMapping("/coin/page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Options contract trading pair list")
    public MessageResult list(PageParam pageParam) {
        Page<ContractCoin> coinList = contractCoinFeign.findAll(pageParam);
        return success(IPage2Page(coinList));
    }

    /**
     * Paginated query
     *
     * @param pageParam
     * @param screen
     * @return
     */
    @PermissionOperation
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Options contract order list")
    public MessageResult pageQuery(
            PageParam pageParam,
            ContractOrderEntrustScreen screen,
            @RequestHeader(SysConstant.SESSION_MEMBER) String authMember
    ) {
        AuthMember user = AuthMember.toAuthMember(authMember);
        Member checkMember = memberFeign.findMemberById(user.getId());
        if (!checkMember.getSuperPartner().equals("1")) {
            return error(messageSource.getMessage("NOT_AN_AGENT"));
        }
        // Get query conditions
        if (screen.getEndTime() != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(screen.getEndTime());
            calendar.add(Calendar.DATE, 1);
            screen.setEndTime(calendar.getTime());
        }
        List<Member> memberList = memberFeign.findPromotionMember(checkMember.getId());
        if (memberList.isEmpty()) {
            return error(messageSource.getMessage("NO_SUBORDINATES"));
        }
        Page<ContractOrderEntrust> all = contractOrderEntrustFeign.findAll4Agent(checkMember.getId(), pageParam, screen);
        return success(IPage2Page(all));
    }

    /**
     * Cancel entrust order
     *
     * @param orderId
     * @return
     */
    @PostMapping("cancel")
    @AccessLog(module = AdminModule.CONTRACTOPTION, operation = "Perpetual contract cancel order")
    public MessageResult cancelOrder(Long orderId) {
        ContractOrderEntrust order = contractOrderEntrustFeign.findOne(orderId);
        if (order == null) {
            return MessageResult.error(messageSource.getMessage("CANCEL_ORDER_FAILED"));
        }
        if (order.getStatus() != ContractOrderEntrustStatus.ENTRUST_ING) {
            return MessageResult.error(messageSource.getMessage("DELEGATE_STATUS_ERROR"));
        }
        // Send message to Exchange system
        rocketMQTemplate.convertAndSend("swap-order-cancel", JSON.toJSONString(order));

        
        return MessageResult.success(messageSource.getMessage("OPERATION_SUCCESS"));
    }
}

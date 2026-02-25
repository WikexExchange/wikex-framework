package com.wikex.wikex.admin.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.dto.CoinprotocolDTO;
import com.wikex.wikex.screen.WithdrawScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.Withdraw;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.CoinprotocolFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.WithdrawFeign;
import com.wikex.wikex.user.vo.WithdrawExcelVO;
import com.wikex.wikex.user.vo.WithdrawVO;
import com.wikex.wikex.util.BindingResultUtil;
import com.wikex.wikex.util.ExcelUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Withdrawal Management
 */
@Slf4j
@RestController
@RequestMapping("/finance/withdraw")
public class WithdrawController extends BaseAdminController {

    @Autowired
    private CoinFeign coinService;

    @Autowired
    private CoinprotocolFeign coinprotocolService;

    @Autowired
    private WithdrawFeign withdrawService;

    @Autowired
    private MemberFeign memberService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("finance:withdraw:coin-list")
    @GetMapping("/coin-list")
    @AccessLog(module = AdminModule.FINANCE, operation = "Get coin list in withdrawal review")
    public MessageResult coinList() {
        List<Coin> list = coinService.getAllCoinNameAndUnit();
        return success(list);
    }

    @RequiresPermissions("finance:withdraw:protocol-list")
    @GetMapping("/protocol-list")
    @AccessLog(module = AdminModule.FINANCE, operation = "Get coin protocol list in withdrawal review")
    public MessageResult protocolList() {
        List<CoinprotocolDTO> list = coinprotocolService.list();
        return success(list);
    }

    @RequiresPermissions("finance:withdraw:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.FINANCE, operation = "Get withdrawal review list")
    public MessageResult pageQuery(WithdrawScreen withdrawScreen,
                                   HttpServletResponse response) throws IOException {

        // Export
        if (withdrawScreen.getIsOut() == 1) {
            List<Withdraw> allOut = withdrawService.findAllOut(withdrawScreen);
            Set<Long> memberSet = new HashSet<>();
            allOut.forEach(v -> memberSet.add(Long.valueOf(v.getMemberId())));
            Map<Long, Member> memberMap = memberService.mapByMemberIds(new ArrayList<>(memberSet));

            List<WithdrawExcelVO> voList = new ArrayList<>();

            allOut.forEach(v -> {
                WithdrawExcelVO vo = new WithdrawExcelVO();
                BeanUtils.copyProperties(v, vo);

                vo.setMemberId(Long.valueOf(v.getMemberId()));
                vo.setMoney(String.valueOf(v.getMoney()));
                vo.setFee(String.valueOf(v.getFee()));
                vo.setReal_money(String.valueOf(v.getRealMoney()));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                vo.setAddtime(sdf.format(new Date(v.getAddTime())));
                if (v.getProcessTime() != null && v.getProcessTime() > 0) {
                    vo.setProcesstime(sdf.format(new Date(v.getProcessTime())));
                } else {
                    vo.setProcesstime("--");
                }

                Integer statusD = v.getStatus();
                String statusStr = "";
                if (statusD == -1) {
                    statusStr = messageSource.getMessage("REJECTED");
                } else if (statusD == 0) {
                    statusStr = messageSource.getMessage("PENDING");
                } else if (statusD == 1) {
                    statusStr = messageSource.getMessage("PROCESSING");
                } else if (statusD == 2) {
                    statusStr = messageSource.getMessage("PROCESSED");
                } else {
                    statusStr = messageSource.getMessage("FAILURE");
                }
                vo.setStatus(statusStr);

                Long memberId = vo.getMemberId();
                if (memberMap.containsKey(memberId)) {
                    Member member = memberMap.get(memberId);
                    vo.setEmail(member.getEmail());
                    vo.setMobilePhone(member.getMobilePhone());
                }
                voList.add(vo);
            });

            ExcelUtil.listToExcel(voList, WithdrawExcelVO.class.getDeclaredFields(), response.getOutputStream());
            return null;
        }

        Page<Withdraw> all = withdrawService.findAll(withdrawScreen);
        List<Long> memberIds = all.getRecords().stream().map(v -> (long) v.getMemberId()).collect(Collectors.toList());
        Map<Long, Member> memberMap = memberService.mapByMemberIds(memberIds);

        Page<WithdrawVO> page = new Page<>();
        BeanUtils.copyProperties(all, page);

        List<WithdrawVO> list = new ArrayList<>();
        for (Withdraw v : all.getRecords()) {
            WithdrawVO withdrawVO = new WithdrawVO();
            BeanUtils.copyProperties(v, withdrawVO);
            Long memberid = (long) withdrawVO.getMemberId();
            if (memberMap.containsKey(memberid)) {
                withdrawVO.setUsername(memberMap.get(memberid).getUsername());
            }
            withdrawVO.setEmail(memberMap.getOrDefault(memberid, new Member()).getEmail());
            list.add(withdrawVO);
        }
        page.setRecords(list);
        return success(IPage2Page(page));
    }

    @RequiresPermissions("finance:withdraw:merge")
    @PostMapping("/merge")
    @AccessLog(module = AdminModule.FINANCE, operation = "Approve/Reject withdrawal")
    public MessageResult merge(@Valid Withdraw withdraw, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }

        if (withdraw.getId() == null || withdraw.getId() <= 0) {
            result = error(messageSource.getMessage("PLEASE_SELECT_RECORD_FOR_REVIEW"));
            return result;
        }

        Withdraw one = withdrawService.findOne(withdraw.getId());
        if (one == null) {
            result = error(messageSource.getMessage("PLEASE_SELECT_RECORD_FOR_REVIEW"));
            return result;
        }

        Withdraw update = new Withdraw();
        update.setId(withdraw.getId());
        update.setStatus(withdraw.getStatus());
        if (withdraw.getStatus() == -1) {
            update.setWithdrawInfo(withdraw.getWithdrawInfo());
        }

        if (StringUtils.isBlank(withdraw.getWithdrawInfo())) {
            update.setWithdrawInfo("");
        }
        update.setProcessTime(new Date().getTime());
        update.setCoinName(one.getCoinName());
        update.setMemberId(one.getMemberId());
        update.setMoney(one.getMoney());
        withdrawService.updateById(update);

        result = success(messageSource.getMessage("OPERATION_SUCCESS"));
        result.setData(withdraw);
        return result;
    }
}

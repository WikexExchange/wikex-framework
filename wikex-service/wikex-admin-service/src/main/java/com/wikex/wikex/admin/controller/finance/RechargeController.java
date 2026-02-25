package com.wikex.wikex.admin.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.dto.CoinprotocolDTO;
import com.wikex.wikex.screen.RechargeScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.Recharge;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.CoinprotocolFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.RechargeFeign;
import com.wikex.wikex.user.vo.RechargeExcelVO;
import com.wikex.wikex.user.vo.RechargeVO;
import com.wikex.wikex.util.ExcelUtil;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Recharge Management
 */
@Slf4j
@RestController
@RequestMapping("/finance/recharge")
public class RechargeController extends BaseAdminController {

    @Autowired
    private CoinFeign coinService;

    @Autowired
    private CoinprotocolFeign coinprotocolService;

    @Autowired
    private RechargeFeign rechargeService;

    @Autowired
    private MemberFeign memberService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @RequiresPermissions("finance:recharge:coin-list")
    @GetMapping("/coin-list")
    @AccessLog(module = AdminModule.FINANCE, operation = "Get coin list in recharge records")
    public MessageResult coinList() {
        List<Coin> list = coinService.getAllCoinNameAndUnit();
        return success(list);
    }

    @RequiresPermissions("finance:recharge:protocol-list")
    @GetMapping("/protocol-list")
    @AccessLog(module = AdminModule.FINANCE, operation = "Get coin protocol list in recharge records")
    public MessageResult protocolList() {
        List<CoinprotocolDTO> list = coinprotocolService.list();
        return success(list);
    }

    @RequiresPermissions("finance:recharge:page-query")
    @PostMapping("/page-query")
    @AccessLog(module = AdminModule.FINANCE, operation = "Get recharge records")
    public MessageResult pageQuery(RechargeScreen rechargeScreen,
                                   HttpServletResponse response) throws IOException {
        // Export
        if (rechargeScreen.getIsOut() == 1) {
            List<Recharge> allOut = rechargeService.findAllOut(rechargeScreen);
            Set<Long> memberSet = new HashSet<>();
            allOut.forEach(v -> memberSet.add(Long.valueOf(v.getMemberId())));
            Map<Long, Member> memberMap = memberService.mapByMemberIds(new ArrayList<>(memberSet));

            List<RechargeExcelVO> voList = new ArrayList<>();

            allOut.forEach(v -> {
                RechargeExcelVO vo = new RechargeExcelVO();
                BeanUtils.copyProperties(v, vo);

                vo.setMemberId(Long.valueOf(v.getMemberId()));
                vo.setMoney(String.valueOf(v.getMoney()));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (v.getAddTime() != null && v.getAddTime() > 0) {
                    vo.setAddtime(sdf.format(new Date(v.getAddTime())));
                } else {
                    vo.setAddtime("--");
                }

                Integer status = v.getStatus();
                String statusStr;
                if (status == 0) {
                    statusStr = messageSource.getMessage("NOT_RECEIVED");
                } else if (status == 1) {
                    statusStr = messageSource.getMessage("RECEIVED");
                } else {
                    statusStr = messageSource.getMessage("FAILURE");
                }
                vo.setStatus(statusStr);

                vo.setConfirms(v.getConfirms() + "/" + v.getNConfirms());

                Long memberId = vo.getMemberId();
                if (memberMap.containsKey(memberId)) {
                    Member member = memberMap.get(memberId);
                    vo.setEmail(member.getEmail());
                    vo.setMobilePhone(member.getMobilePhone());
                }
                voList.add(vo);
            });

            ExcelUtil.listToExcel(voList, RechargeExcelVO.class.getDeclaredFields(), response.getOutputStream());
            return null;
        }

        Page<Recharge> all = rechargeService.findAll(rechargeScreen);

        List<Long> memberIds = all.getRecords().stream().map(v -> (long) v.getMemberId()).collect(Collectors.toList());
        Map<Long, Member> memberMap = memberService.mapByMemberIds(memberIds);

        Page<RechargeVO> page = new Page<>();
        BeanUtils.copyProperties(all, page);

        List<RechargeVO> list = new ArrayList<>();
        for (Recharge v : all.getRecords()) {
            RechargeVO rechargeVO = new RechargeVO();
            BeanUtils.copyProperties(v, rechargeVO);
            Long memberid = (long) rechargeVO.getMemberId();
            if (memberMap.containsKey(memberid)) {
                rechargeVO.setUsername(memberMap.get(memberid).getUsername());
            }
            list.add(rechargeVO);
        }

        return success(IPage2Page(page));
    }
}

package com.wikex.wikex.user.controller;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static com.wikex.wikex.constant.SysConstant.SESSION_MEMBER;

@Api(tags = "Spot")
@RestController
@RequestMapping("/spot")
@Slf4j
public class SpotController extends BaseController {

    @Autowired
    private LocaleMessageSourceService sourceService;

    @Autowired
    private EquitySnapshotService equitySnapshotService;

    @ApiOperation(value = "Spot PnL Summary")
    @PermissionOperation
    @GetMapping("pnl/summary")
    public MessageResult portfolioPnlSummary(@RequestHeader(SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        Map<String, Object> data = equitySnapshotService.getSummary(memberId);

        MessageResult mr = MessageResult.success("success");
        mr.setData(data);
        return mr;
    }

    @ApiOperation(value = "Cumulative PnL")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "days", value = "7, 30, 90, 120, 150, 180")
    })
    @PermissionOperation
    @GetMapping("pnl/cumulative")
    public MessageResult cumulativePnl(@RequestHeader(SESSION_MEMBER) String authMember,
            @RequestParam("days") Integer days) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        if (days == null || days <= 0) {
            return MessageResult.error(sourceService.getMessage("INVALID_RANGE"));
        }

        boolean valid = days != 7 && days != 30 && days != 90 && days != 120 && days != 150 && days != 180;
        if (valid) {
            return MessageResult.error(sourceService.getMessage("INVALID_RANGE"));
        }

        Map<String, Object> result = equitySnapshotService.getCumulativePnl(memberId, days);
        MessageResult mr = MessageResult.success("success");

        mr.setData(result);
        return mr;
    }

    @ApiOperation(value = "Get Daily Pnl Chart")
    @PermissionOperation
    @GetMapping("pnl/daily/chart")
    public MessageResult getDailyChart(@RequestHeader(SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        List<Map<String, Object>> result = equitySnapshotService.getDailyChart(memberId);

        MessageResult mr = MessageResult.success("success");
        mr.setData(result);
        return mr;
    }

    @ApiOperation(value = "Get Daily Pnl Calender")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "view", value = "month, year"),
            @ApiImplicitParam(name = "year", value = "Year"),
            @ApiImplicitParam(name = "month", value = "Month")
    })
    @PermissionOperation
    @GetMapping("pnl/daily/calendar")
    public MessageResult getDailyCalendar(@RequestHeader(SESSION_MEMBER) String authMember,
            @RequestParam(value = "view", required = true) String view,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        String viewType = view == null ? "" : view.trim().toLowerCase();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        List<Map<String, Object>> result;

        switch (viewType) {
            case "month": {
                int targetYear = year != null ? year : today.getYear();
                int targetMonth = month != null ? month : today.getMonthValue();

                if (targetMonth < 1 || targetMonth > 12) {
                    return MessageResult.error(sourceService.getMessage("INVALID_RANGE"));
                }

                result = equitySnapshotService.getDailyCalendarMonth(memberId, targetYear, targetMonth);
                break;
            }
            case "year": {
                int targetYear = year != null ? year : today.getYear();
                result = equitySnapshotService.getDailyCalendarYear(memberId, targetYear);
                break;
            }
            default:
                return MessageResult.error(sourceService.getMessage("INVALID_RANGE"));
        }

        MessageResult mr = MessageResult.success("success");
        mr.setData(result);
        return mr;
    }

    @ApiOperation(value = "Get Spot Equity Trend")
    @PermissionOperation
    @GetMapping("pnl/equity-trend")
    public MessageResult getSpotEquityTrend(@RequestHeader(SESSION_MEMBER) String authMember) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        List<Map<String, Object>> result = equitySnapshotService.getSpotEquityTrend(memberId, 7);

        MessageResult mr = MessageResult.success("success");
        mr.setData(result);
        return mr;
    }

    @ApiOperation(value = "Get Spot Asset Breakdown")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "topLimit", value = "Top n assets", defaultValue = "4")
    })
    @PermissionOperation
    @GetMapping("pnl/asset-breakdown")
    public MessageResult getSpotAssetBreakdown(@RequestHeader(SESSION_MEMBER) String authMember,
            @RequestParam(value = "topLimit", defaultValue = "4") int topLimit) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Long memberId = member.getId();

        Map<String, Object> result = equitySnapshotService.getSpotAssetBreakdown(memberId, topLimit);

        MessageResult mr = MessageResult.success("success");
        mr.setData(result);
        return mr;
    }
}

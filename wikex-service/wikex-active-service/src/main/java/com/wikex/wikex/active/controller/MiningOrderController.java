package com.wikex.wikex.active.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.active.service.MiningOrderDetailService;
import com.wikex.wikex.active.service.MiningOrderService;
import com.wikex.wikex.active.entity.MiningOrder;
import com.wikex.wikex.active.entity.MiningOrderDetail;
import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.transform.AuthMember;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Api(tags = "Innovation Lab - Mining Machines")
@RestController
@RequestMapping("miningorder")
public class MiningOrderController extends BaseController {
    @Autowired
    private MiningOrderService miningOrderService;

    @Autowired
    private MiningOrderDetailService miningOrderDetailService;

    @Autowired
    private LocaleMessageSourceService sourceService;

    /**
     * My mining machine list
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "My mining machine list")
    @PermissionOperation
    @RequestMapping("my-minings")
    public MessageResult page(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                              @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {

        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        MessageResult mr = new MessageResult();
//      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        IPage<MiningOrder> all = miningOrderService.findAllByMemberIdPage(member.getId(), pageNo, pageSize);
        long currentTime = DateUtil.getCurrentDate().getTime();
        for (int i = 0; i < all.getRecords().size(); i++) {
            // Timed out
            if (currentTime > all.getRecords().get(i).getEndTime().getTime()) {
                all.getRecords().get(i).setMiningStatus(2); // Ended
            }
            // Exceeded allotted days
            if (all.getRecords().get(i).getMiningedDays() >= all.getRecords().get(i).getMiningDays()) {
                all.getRecords().get(i).setMiningStatus(2); // Ended
            }
        }
        mr.setCode(0);
        mr.setData(IPage2Page(all));
        return mr;
    }

    /**
     * Get specified mining machine details
     * @param miningId
     * @return
     */
    @ApiOperation(value = "Get specified mining machine details")
    @PermissionOperation
    @RequestMapping("my-mining-detail")
    public MessageResult miningDetail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                      Long miningId) throws ParseException {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        Assert.notNull(miningId, sourceService.getMessage("MINING_MACHINE_NOT_FOUND"));
        MiningOrder mo = miningOrderService.getById(miningId);
        if (mo != null) {
            if (mo.getMemberId().longValue() != member.getId()) {
                return error(sourceService.getMessage("ILLEGAL_ACCESS"));
            }
            long currentTime = DateUtil.getCurrentDate().getTime();
            if (currentTime > mo.getEndTime().getTime()) {
                mo.setMiningStatus(2);
            }
            if (mo.getMiningedDays() >= mo.getMiningDays()) {
                mo.setMiningStatus(2);
            }
            return success(mo);
        } else {
            return error(sourceService.getMessage("MINING_MACHINE_NOT_FOUND"));
        }
    }

    /**
     * Mining machine output details
     * @param miningId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "Mining machine output details")
    @PermissionOperation
    @RequestMapping("mining-detail")
    public MessageResult miningDetail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                      @RequestParam(value = "miningId", defaultValue = "1") Long miningId,
                                      @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        Assert.notNull(miningId, sourceService.getMessage("MINING_MACHINE_NOT_FOUND"));
        MiningOrder mining = miningOrderService.getById(miningId);
        Assert.notNull(mining, sourceService.getMessage("MINING_MACHINE_NOT_FOUND"));
        if (mining.getMemberId().longValue() != member.getId()) {
            return error(sourceService.getMessage("ILLEGAL_ACCESS"));
        }
        IPage<MiningOrderDetail> all = miningOrderDetailService.findAllByMiningOrderId(miningId, pageNo, pageSize);
        return success(IPage2Page(all));
    }
}

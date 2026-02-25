package com.wikex.wikex.active.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.active.entity.LockedOrder;
import com.wikex.wikex.active.entity.LockedOrderDetail;
import com.wikex.wikex.active.service.LockedOrderDetailService;
import com.wikex.wikex.active.service.LockedOrderService;
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
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@Api(tags = "Innovation Lab - Locking")
@RestController
@RequestMapping("lockedorder")
public class LockedOrderController extends BaseController {
    @Autowired
    private LockedOrderService lockedOrderService;

    @Autowired
    private LockedOrderDetailService lockedOrderDetailService;

    @Autowired
    private LocaleMessageSourceService sourceService;

    /**
     * My locked positions list
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "My locked positions list")
    @PermissionOperation
    @RequestMapping("my-locked")
    public MessageResult page(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                              @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                              @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) throws ParseException {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        MessageResult mr = new MessageResult();
        IPage<LockedOrder> all = lockedOrderService.findAllByMemberIdPage(member.getId(), pageNo, pageSize);
        long currentTime = DateUtil.getCurrentDate().getTime();
        for(int i = 0; i < all.getRecords().size(); i++) {
            // Timed out
            if(currentTime > all.getRecords().get(i).getEndTime().getTime()) {
                all.getRecords().get(i).setLockedStatus(2); // Ended
            }
            // Exceeded days
            if(all.getRecords().get(i).getReleasedDays() >= all.getRecords().get(i).getLockedDays()) {
                all.getRecords().get(i).setLockedStatus(2); // Ended
            }
        }
        mr.setCode(0);
        mr.setData(IPage2Page(all));
        return mr;
    }

    /**
     * Get specified lock details
     * @param miningId
     * @return
     */
    @ApiOperation(value = "Lock details")
    @PermissionOperation
    @RequestMapping("my-locked-detail")
    public MessageResult miningDetail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember, Long miningId) throws ParseException {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        Assert.notNull(miningId, sourceService.getMessage("LOCK_NOT_FOUND"));
        LockedOrder mo = lockedOrderService.getById(miningId);
        if(mo != null) {
            if(mo.getMemberId().longValue() != member.getId()) {
                return error(sourceService.getMessage("ILLEGAL_ACCESS"));
            }
            long currentTime = DateUtil.getCurrentDate().getTime();

            if(currentTime > mo.getEndTime().getTime()) {
                mo.setLockedStatus(2);
            }
            if(mo.getReleasedDays() >= mo.getLockedDays()) {
                mo.setLockedStatus(2);
            }
            return success(mo);
        }else {
            return error(sourceService.getMessage("LOCK_NOT_FOUND"));
        }
    }

    /**
     * Mining output details
     * @param miningId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "Mining output details")
    @PermissionOperation
    @RequestMapping("mining-detail")
    public MessageResult miningDetail(@RequestHeader(SysConstant.SESSION_MEMBER) String authMember,
                                      @RequestParam(value = "miningId", defaultValue = "1") Long miningId,
                                      @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        AuthMember member = AuthMember.toAuthMember(authMember);
        Assert.notNull(member, "The login timeout!");
        Assert.notNull(miningId, sourceService.getMessage("LOCK_NOT_FOUND"));
        LockedOrder mining = lockedOrderService.getById(miningId);
        Assert.notNull(mining, sourceService.getMessage("LOCK_NOT_FOUND"));
        if(mining.getMemberId().longValue() != member.getId()) {
            return error(sourceService.getMessage("ILLEGAL_ACCESS"));
        }
        IPage<LockedOrderDetail> all = lockedOrderDetailService.findAllByMiningOrderId(miningId, pageNo, pageSize);
        return success(IPage2Page(all));
    }
}

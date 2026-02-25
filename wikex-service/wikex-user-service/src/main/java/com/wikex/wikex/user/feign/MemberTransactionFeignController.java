package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.MemberTransactionScreen;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.service.MemberTransactionService;
import com.wikex.wikex.user.vo.MemberTransactionVO;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/memberTransactionFeign")
public class MemberTransactionFeignController extends BaseController {

    @Autowired
    private MemberTransactionService memberTransactionService;

    @PostMapping("save")
    public MessageResult save(@RequestBody MemberTransaction memberTransaction) {
        if (memberTransaction.getCreateTime() == null) {
            memberTransaction.setCreateTime(new Date());
        }
        boolean ret = memberTransactionService.saveOrUpdate(memberTransaction);
        if (ret) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("joinFind")
    public Page<MemberTransactionVO> joinFind(@RequestBody MemberTransactionScreen screen) {
        return memberTransactionService.joinFind(screen);
    }

    @PostMapping("findOne")
    public MemberTransaction findOne(@RequestBody Long id) {
        return memberTransactionService.findOne(id);
    }

    @PostMapping("deleteHistory")
    public int deleteHistory(@RequestParam("startTime") Date startTime) {
        return memberTransactionService.deleteHistory(startTime);
    }

    @PostMapping("updateRewardRobot")
    void updateRewardRobot() {
        memberTransactionService.updateRewardRobot();
    }

    @PostMapping("sendExchangeReward")
    void sendExchangeReward() {
        memberTransactionService.sendExchangeReward();
    }

    @PostMapping("sendSecondReward")
    void sendSecondReward() {
        memberTransactionService.sendSecondReward();
    }

    @PostMapping("sendOptionReward")
    void sendOptionReward() {
        memberTransactionService.sendOptionReward();
    }

}

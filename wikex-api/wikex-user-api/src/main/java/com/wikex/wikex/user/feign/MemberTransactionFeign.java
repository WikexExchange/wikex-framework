package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberTransactionScreen;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.vo.MemberTransactionVO;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-user",contextId = "memberTransactionFeign")
public interface MemberTransactionFeign {

    @PostMapping("/memberTransactionFeign/save")
    MessageResult save(@RequestBody MemberTransaction memberTransaction);

    @PostMapping("/memberTransactionFeign/joinFind")
    Page<MemberTransactionVO> joinFind(@RequestBody MemberTransactionScreen screen);

    @PostMapping("/memberTransactionFeign/findOne")
    MemberTransaction findOne(@RequestBody Long id);

    @PostMapping("/memberTransactionFeign/deleteHistory")
    int deleteHistory(@RequestParam("startTime") Date startTime);

    @PostMapping("/memberTransactionFeign/updateRewardRobot")
    void updateRewardRobot();

    @PostMapping("/memberTransactionFeign/sendExchangeReward")
    void sendExchangeReward();

    @PostMapping("/memberTransactionFeign/sendSecondReward")
    void sendSecondReward();

    @PostMapping("/memberTransactionFeign/sendOptionReward")
    void sendOptionReward();
}

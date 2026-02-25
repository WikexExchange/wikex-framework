package com.wikex.wikex.user.feign;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.AgentWallet;
import com.wikex.wikex.user.service.AgentWalletService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@Api(tags = "Agent Reward Wallet")
@RestController
@Slf4j
@RequestMapping(value = "/agentWalletFeign", method = RequestMethod.POST)
public class AgentWalletFeignController extends BaseController {

    @Autowired
    private AgentWalletService agentWalletService;

    @PostMapping("findWalletByMemberIdAndCoinUnit")
    AgentWallet findWalletByMemberIdAndCoinUnit(@RequestParam("memberId")Long memberId, @RequestParam("coinUnit")String coinUnit){
        return agentWalletService.findWalletByMemberIdAndCoinUnit(memberId,coinUnit);
    }

    @PostMapping("increaseBalance")
    void increaseBalance(@RequestParam("id")Long id, @RequestParam("reward") BigDecimal reward){
        agentWalletService.increaseBalance(id,reward);
    }

}

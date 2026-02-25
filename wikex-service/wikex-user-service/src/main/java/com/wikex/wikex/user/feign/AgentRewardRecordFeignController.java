package com.wikex.wikex.user.feign;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.service.AgentRewardRecordService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * @date 2020-12-19
 */
@Api(tags = "Agent Reward Record")
@RestController
@Slf4j
@RequestMapping(value = "/agentRewardRecordFeign", method = RequestMethod.POST)
public class AgentRewardRecordFeignController extends BaseController {

    @Autowired
    private AgentRewardRecordService agentRewardRecordService;

    @PostMapping("saveAgentRewardRecord")
    void saveAgentRewardRecord(@RequestParam("memberId")Long memberId, @RequestParam("upMemberId")Long upMemberId,
                               @RequestParam("reward") BigDecimal reward, @RequestParam("unit")String unit,
                               @RequestParam("type")int type, @RequestParam("orderId")Long orderId){
        agentRewardRecordService.saveAgentRewardRecord(memberId,upMemberId,reward,unit,type,orderId);

    }

}

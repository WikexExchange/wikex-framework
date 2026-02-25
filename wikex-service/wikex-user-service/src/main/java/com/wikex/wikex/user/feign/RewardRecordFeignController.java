package com.wikex.wikex.user.feign;


import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.RewardRecord;
import com.wikex.wikex.user.service.RewardRecordService;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rewardRecordFeign")
public class RewardRecordFeignController extends BaseController {

    @Autowired
    private RewardRecordService rewardRecordService;

    @PostMapping("save")
    public MessageResult save(@RequestBody RewardRecord rewardRecord){
        boolean ret = rewardRecordService.save(rewardRecord);
        if (ret) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

}


package com.wikex.wikex.user.feign;

import com.wikex.wikex.user.entity.RewardRecord;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "wikex-user",contextId = "rewardRecordFeign")
public interface RewardRecordFeign {

    @PostMapping("/rewardRecordFeign/save")
    MessageResult save(@RequestBody RewardRecord rewardRecord);

}

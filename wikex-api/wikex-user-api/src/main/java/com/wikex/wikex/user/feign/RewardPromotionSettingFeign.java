package com.wikex.wikex.user.feign;

import com.wikex.wikex.user.entity.RewardPromotionSetting;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "wikex-user",contextId = "rewardPromotionSettingFeign")
public interface RewardPromotionSettingFeign {

    @GetMapping("/rewardPromotionSettingFeign/findByType")
    RewardPromotionSetting findByType(@RequestParam("type") Integer type);

}

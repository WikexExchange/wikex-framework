package com.wikex.wikex.user.feign;


import com.wikex.wikex.constant.PromotionRewardType;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.RewardPromotionSetting;
import com.wikex.wikex.user.service.RewardPromotionSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rewardPromotionSettingFeign")
public class RewardPromotionSettingFeignController extends BaseController {

    @Autowired
    private RewardPromotionSettingService rewardPromotionSettingService;

    @GetMapping("findByType")
    public RewardPromotionSetting findByType(@RequestParam("type") Integer type) {
        PromotionRewardType pType = PromotionRewardType.creator(type);
        RewardPromotionSetting rewardPromotionSetting = rewardPromotionSettingService.findByType(pType);
        return rewardPromotionSetting;
    }


}


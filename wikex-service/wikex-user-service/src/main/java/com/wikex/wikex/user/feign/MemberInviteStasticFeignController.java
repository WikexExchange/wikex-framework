package com.wikex.wikex.user.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.MemberInviteStasticScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.MemberInviteStastic;
import com.wikex.wikex.user.service.MemberInviteStasticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/memberInviteStasticFeign")
public class MemberInviteStasticFeignController extends BaseController {

    @Autowired
    private LocaleMessageSourceService messageSourceService;

    @Autowired
    private MemberInviteStasticService memberInviteStasticService;



    @RequestMapping(value = "queryRankList")
    public Page<MemberInviteStastic> queryRankList(@RequestBody MemberInviteStasticScreen screen){
        Page<MemberInviteStastic> page = memberInviteStasticService.queryRankList(screen);
        return page;
    }


}


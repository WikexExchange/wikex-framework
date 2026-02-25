package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberInviteStasticScreen;
import com.wikex.wikex.user.entity.MemberInviteStastic;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-user",contextId = "memberInviteStasticFeign")
public interface MemberInviteStasticFeign {


    @RequestMapping(value = "/memberInviteStasticFeign/queryRankList")
    Page<MemberInviteStastic> queryRankList(@RequestBody MemberInviteStasticScreen screen);
}

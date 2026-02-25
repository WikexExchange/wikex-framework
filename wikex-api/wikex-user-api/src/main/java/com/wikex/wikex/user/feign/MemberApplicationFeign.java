package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberApplicationScreen;
import com.wikex.wikex.user.entity.MemberApplication;
import com.wikex.wikex.user.vo.MemberApplicationVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-user",contextId = "memberApplicationFeign")
public interface MemberApplicationFeign {

    @PostMapping(value = "/memberApplicationFeign/findAll")
    Page<MemberApplicationVo> findAll(@RequestBody MemberApplicationScreen screen);

    @PostMapping(value = "/memberApplicationFeign/fetch")
    List<MemberApplication> fetch();

    @GetMapping(value = "/memberApplicationFeign/findById")
    MemberApplication findById(@RequestParam("id") Long id);

    @PostMapping(value = "/memberApplicationFeign/auditPass")
    void auditPass(@RequestBody MemberApplication application);

    @PostMapping(value = "/memberApplicationFeign/auditNotPass")
    void auditNotPass(@RequestBody MemberApplication application);

    @PostMapping(value = "/memberApplicationFeign/countAuditing")
    Integer countAuditing();
}

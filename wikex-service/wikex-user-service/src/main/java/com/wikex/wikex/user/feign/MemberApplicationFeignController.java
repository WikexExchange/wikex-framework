package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.MemberApplicationScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.entity.MemberApplication;
import com.wikex.wikex.user.service.MemberApplicationService;
import com.wikex.wikex.user.vo.MemberApplicationVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "Real-name verification form")
@RestController
@RequestMapping("memberApplicationFeign")
public class MemberApplicationFeignController extends BaseController {
    @Autowired
    private MemberApplicationService memberApplicationService;
    @Autowired
    private LocaleMessageSourceService messageSource;




    @PostMapping(value = "/findAll")
    public Page<MemberApplicationVo> findAll(@RequestBody MemberApplicationScreen screen){
        return memberApplicationService.findAll(screen);
    }






    @GetMapping(value = "/findById")
    public MemberApplication findById(@RequestParam("id") Long id){
        return memberApplicationService.getById(id);
    }

    @PostMapping(value = "/fetch")
    public List<MemberApplication> fetch(){
        return memberApplicationService.list();
    };


    @PostMapping(value = "/auditPass")
    void auditPass(@RequestBody MemberApplication application){
        memberApplicationService.auditPass(application);
    }


    @PostMapping(value = "/auditNotPass")
    void auditNotPass(@RequestBody MemberApplication application){
        memberApplicationService.auditNotPass(application);
    }

    @PostMapping(value = "/countAuditing")
    Integer countAuditing(){
        return memberApplicationService.countAuditing();
    }
}

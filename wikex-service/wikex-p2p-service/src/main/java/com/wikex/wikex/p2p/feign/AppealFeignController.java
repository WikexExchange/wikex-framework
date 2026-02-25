package com.wikex.wikex.p2p.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.p2p.entity.Appeal;
import com.wikex.wikex.p2p.service.AppealService;
import com.wikex.wikex.p2p.vo.AppealVo;
import com.wikex.wikex.screen.AppealScreen;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/appealFeign")
@Slf4j
public class AppealFeignController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(AppealFeignController.class);

    @Autowired
    private AppealService appealService;


    @PostMapping("appealQuery")
    public Page appealQuery(@RequestBody AppealScreen screen){
        return appealService.appealQuery(screen);
    }

    @PostMapping("findOneAppealVO")
    public AppealVo findOneAppealVO(@RequestParam("id") Long id){
        return appealService.findOneAppealVO(id);
    }

    @PostMapping("findOne")
    public Appeal findOne(@RequestParam("id") Long id){
        return appealService.findOne(id);
    }

    @PostMapping("updateById")
    public MessageResult updateById(@RequestBody Appeal appeal){
        appealService.updateById(appeal);
        return success();
    }

    @PostMapping("countAuditing")
    public Integer countAuditing(){
        return appealService.countAuditing();
    }
}

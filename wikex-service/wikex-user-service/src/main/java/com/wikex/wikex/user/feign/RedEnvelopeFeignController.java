package com.wikex.wikex.user.feign;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.user.entity.RedEnvelope;
import com.wikex.wikex.user.service.RedEnvelopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/redEnvelopeFeign")
public class RedEnvelopeFeignController extends BaseController {

    @Autowired
    private RedEnvelopeService rechargeService;

    @PostMapping("/findAll")
    public Page<RedEnvelope> findAll(@RequestBody PageParam pageParam){
        LambdaQueryWrapper<RedEnvelope> query = new LambdaQueryWrapper<>();
        Page<RedEnvelope> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        return rechargeService.page(page);
    }

    @PostMapping("/findOne")
    RedEnvelope findOne(@RequestParam("id") Long id){
        return rechargeService.getById(id);
    }

    @PostMapping("/save")
    RedEnvelope save(@RequestBody RedEnvelope redEnvelope){
        rechargeService.saveOrUpdate(redEnvelope);
        return redEnvelope;
    }


}


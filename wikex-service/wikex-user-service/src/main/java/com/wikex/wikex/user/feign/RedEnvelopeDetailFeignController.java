package com.wikex.wikex.user.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.RedEnvelopeDetail;
import com.wikex.wikex.user.service.RedEnvelopeDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/redEnvelopeDetailFeignController")
public class RedEnvelopeDetailFeignController extends BaseController {

    @Autowired
    private RedEnvelopeDetailService redEnvelopeDetailService;

    @PostMapping("/findByEnvelope")
    Page<RedEnvelopeDetail> findByEnvelope(@RequestParam("envelopeId") Long envelopeId,
                                           @RequestParam("pageNo")Integer pageNo,
                                           @RequestParam("pageSize")Integer pageSize){
        return redEnvelopeDetailService.findByEnvelope(envelopeId,pageNo,pageSize);
    }


}


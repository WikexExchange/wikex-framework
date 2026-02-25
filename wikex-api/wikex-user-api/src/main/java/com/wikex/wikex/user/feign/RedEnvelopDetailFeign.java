package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.user.entity.RedEnvelopeDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "wikex-user",contextId = "redEnvelopDetailFeign")
public interface RedEnvelopDetailFeign {

    @PostMapping("/redEnvelopeDetailFeignController/findByEnvelope")
    Page<RedEnvelopeDetail> findByEnvelope(@RequestParam("envelopeId") Long envelopeId,
                                           @RequestParam("pageNo")Integer pageNo,
                                           @RequestParam("pageNo")Integer pageSize);
}

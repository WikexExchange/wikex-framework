package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.user.entity.RedEnvelope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "wikex-user",contextId = "redEnvelopeFeign")
public interface RedEnvelopeFeign {

    @PostMapping("/redEnvelopeFeign/findAll")
    Page<RedEnvelope> findAll(@RequestBody PageParam pageParam);

    @PostMapping("/redEnvelopeFeign/findOne")
    RedEnvelope findOne(@RequestParam("id") Long id);

    @PostMapping("/redEnvelopeFeign/save")
    RedEnvelope save(@RequestBody RedEnvelope redEnvelope);
}

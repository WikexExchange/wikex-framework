package com.wikex.wikex.user.feign;

import com.wikex.wikex.user.entity.MemberApiKey;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;



@FeignClient(value = "wikex-user",contextId = "memberApiKeyFeign")
public interface MemberApiKeyFeign {

    @PostMapping(value = "/memberApiKeyFeign/findMemberApiKeyByApiKey")
    MemberApiKey findMemberApiKeyByApiKey(@RequestParam("apiKey")String apiKey);

}

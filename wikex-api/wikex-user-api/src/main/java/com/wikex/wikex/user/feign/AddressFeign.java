package com.wikex.wikex.user.feign;

import com.wikex.wikex.user.entity.Addressext;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;



@FeignClient(value = "wikex-user",contextId = "addressFeign")
public interface AddressFeign{

    @PostMapping(value = "/addressFeign/findByAddress")
    Addressext findByAddress(@RequestParam("address")String address);

    @PostMapping(value = "/addressFeign/save")
    Addressext save(@RequestBody Addressext addressext);
}

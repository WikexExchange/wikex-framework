package com.wikex.wikex.admin.feign;

import com.wikex.wikex.admin.entity.DataDictionary;
import com.wikex.wikex.screen.ExchangeCoinScreen;
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
@FeignClient(value = "wikex-admin",contextId = "dataDictionaryFeign")
public interface DataDictionaryFeign {

    @PostMapping("/dataDictionaryFeign/findByBond")
    DataDictionary findByBond(@RequestParam("bond")String bond);
}

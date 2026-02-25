package com.wikex.wikex.p2p.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-p2p",contextId = "otcCoinFeign")
public interface OtcCoinFeign {
    @PostMapping(value = "/otcCoinFeign/findOne")
    OtcCoin findOne(@RequestParam("id") Long id);

    @PostMapping(value = "/otcCoinFeign/save")
    MessageResult save(@RequestBody OtcCoin otcCoin);

    @PostMapping(value = "/otcCoinFeign/findAll")
    List<OtcCoin> findAll();

    @PostMapping(value = "/otcCoinFeign/deletes")
    MessageResult deletes(@RequestParam(value = "ids")Long[] ids);

    @PostMapping(value = "/otcCoinFeign/findAllPage")
    Page<OtcCoin> findAllPage(PageParam pageParam);

    @PostMapping(value = "/otcCoinFeign/findAllUnits")
    List<String> findAllUnits();

    @PostMapping(value = "/otcCoinFeign/findByUnit")
    OtcCoin findByUnit(@RequestParam(value = "coinUnit")String coinUnit);
}

package com.wikex.wikex.p2p.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;



@RestController
@Slf4j
@RequestMapping(value = "/otcCoinFeign")
public class OtcCoinFeignController extends BaseController{

    @Autowired
    private OtcCoinService coinService;

    @PostMapping(value = "findOne")
    public OtcCoin findOne(@RequestParam("id") Long id){
        return coinService.getById(id);
    }
    @PostMapping(value = "save")
    public MessageResult save(@RequestBody OtcCoin otcCoin){
        coinService.saveOrUpdate(otcCoin);
        return MessageResult.success();
    }

    @PostMapping(value = "findAll")
    public List<OtcCoin> findAll(){
        return coinService.list();
    }

    @PostMapping(value = "deletes")
    public MessageResult deletes(@RequestParam(value = "ids")Long[] ids){
        List<Long> idList = new ArrayList<>();
        for (Long id : ids) {
            idList.add(id);
        }
        coinService.removeByIds(idList);
        return success();
    }

    @PostMapping(value = "findAllPage")
    public Page<OtcCoin> findAllPage(PageParam pageParam){
        return coinService.findAllPage(pageParam);
    }


    @PostMapping(value = "findAllUnits")
    public List<String> findAllUnits(){
        return coinService.findAllUnits();
    }


    @PostMapping(value = "findByUnit")
    public OtcCoin findByUnit(@RequestParam(value = "coinUnit")String coinUnit){
        return coinService.findByUnit(coinUnit);
    }


}

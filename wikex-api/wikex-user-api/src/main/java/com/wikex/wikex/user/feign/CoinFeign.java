package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.wikex.wikex.user.dto.ContractDTO;
import com.wikex.wikex.user.entity.Coin;
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
@FeignClient(value = "wikex-user",contextId = "coinFeign")
public interface CoinFeign {


    @GetMapping("/coinFeign/findByUnit")
    Coin findByUnit(@RequestParam("coinUnit")String coinUnit);

    @GetMapping(value = "/coinFeign/findAll")
    Page<Coin> findAll(@RequestParam("pageNo")Integer pageNo, @RequestParam("pageSize")Integer pageSize);

    @PostMapping(value = "/coinFeign/save")
    Boolean save(@RequestBody Coin coin);

    @PostMapping(value = "/coinFeign/getAllCoinNameAndUnit")
    List<Coin> getAllCoinNameAndUnit();

    @GetMapping("/coinFeign/findByCoinId")
    Coin findByCoinId(@RequestParam("coinId")String coinId);

    @GetMapping("/coinFeign/getContractByProtocol")
    List<ContractDTO> getContractByProtocol(@RequestParam(value = "protocol")String protocol);

    @PostMapping(value = "/coinFeign/getAllCoinName")
    List<String> getAllCoinName() ;

}

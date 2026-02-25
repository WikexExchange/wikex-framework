package com.wikex.wikex.user.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.dto.CoinDTO;
import com.wikex.wikex.user.dto.ContractDTO;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.service.CoinService;
import com.wikex.wikex.user.service.CoinextService;
import com.wikex.wikex.user.service.MemberWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/coinFeign")
public class CoinFeignController extends BaseController {

    @Autowired
    private CoinService coinService;
    @Autowired
    private CoinextService coinextService;
    @Autowired
    private MemberWalletService memberWalletService;




    @GetMapping("findByUnit")
    public Coin findByUnit(@RequestParam("coinUnit")String coinUnit) {
        Coin coin = coinService.findByUnit(coinUnit);
        return coin;
    }


    @GetMapping(value = "/findAll")
    public Page<Coin> findAll(@RequestParam("pageNo")Integer pageNo, @RequestParam("pageSize")Integer pageSize){
        return coinService.findAll(pageNo,pageSize);
    }

    @PostMapping(value = "/save")
    public Boolean save(@RequestBody Coin coin){
        if(coin==null){
            return false;
        }
        if (coin.getId()==null) {
            Long maxId = coinService.getMaxId();
            coin.setId(maxId+1);
        }
        return coinService.saveOrUpdate(coin);
    }


    @PostMapping(value = "/getAllCoinNameAndUnit")
    public List<CoinDTO> getAllCoinNameAndUnit() {
        List<CoinDTO> allNameAndUnit = coinService.findAllNameAndUnit();
        return allNameAndUnit;
    }

    @PostMapping(value = "/getAllCoinName")
    public List<String> getAllCoinName() {
        List<String> list = coinService.getAllCoinName();
        return list;
    }


    @GetMapping("findByCoinId")
    public Coin findByCoinId(@RequestParam("coinId")String coinId){
        return coinService.getById(coinId);
    }

    @GetMapping("getContractByProtocol")
    public List<ContractDTO> getContractByProtocol(@RequestParam(value = "protocol")String protocol){
        List<ContractDTO> list=coinService.getContractByProtocol(protocol);
        return list;
    }






}


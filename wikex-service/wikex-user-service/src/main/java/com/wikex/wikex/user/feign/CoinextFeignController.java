
package com.wikex.wikex.user.feign;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.CoinextScreen;
import com.wikex.wikex.user.entity.Coinext;
import com.wikex.wikex.user.service.CoinextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * Member User Front-End Controller
 * </p>
 *
 * @author markchao
 * @since 2021-06-14
 */
@RestController
@RequestMapping("/coinextFeign")
public class CoinextFeignController extends BaseController {

    @Autowired
    private CoinextService coinextService;

    @PostMapping(value = "/findAll")
    public Page<Coinext> findAll(@RequestBody CoinextScreen coinextScreen){
        return coinextService.findAll(coinextScreen);
    }


    @GetMapping(value = "/findFirstByCoinNameAndProtocol")
    public Coinext findFirstByCoinNameAndProtocol(@RequestParam("coinName")String coinName, @RequestParam("protocol")Integer protocol){
        return coinextService.findFirstByCoinNameAndProtocol(coinName,protocol);
    }

    @PostMapping(value = "/save")
    Coinext save(@RequestBody Coinext coinext){
        coinextService.saveOrUpdate(coinext);
        return coinext;
    }


}


package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.dto.CoinprotocolDTO;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.service.CoinprotocolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coinprotocolFeign")
public class CoinprotocolFeignController extends BaseController {

    @Autowired
    private CoinprotocolService coinprotocolService;

    @PostMapping(value = "/list")
    public List<CoinprotocolDTO> list() {
        List<CoinprotocolDTO> list = coinprotocolService.allCoinprotocolList();
        return list;
    }

    @PostMapping(value = "/findByProtocol")
    public Coinprotocol findByProtocol(@RequestParam("protocol") Integer protocol) {
        Coinprotocol coinprotocol = coinprotocolService.findByProtocol(protocol);
        return coinprotocol;
    }

    @GetMapping(value = "/findAll")
    public Page<Coinprotocol> findAll(@RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize) {
        return coinprotocolService.findAll(pageNo, pageSize);
    }

    @PostMapping(value = "/save")
    public Coinprotocol save(@RequestBody Coinprotocol coinprotocol) {
        coinprotocolService.saveOrUpdate(coinprotocol);
        return coinprotocol;
    }

    @PostMapping(value = "/findBySymbol")
    public Coinprotocol findBySymbol(@RequestParam("symbol") String symbol) {
        LambdaQueryWrapper<Coinprotocol> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coinprotocol::getSymbol, symbol);
        List<Coinprotocol> list = coinprotocolService.list(queryWrapper);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

}

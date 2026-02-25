package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.dto.CoinprotocolDTO;
import com.wikex.wikex.user.entity.Coinprotocol;
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
@FeignClient(value = "wikex-user",contextId = "coinprotocolFeign")
public interface CoinprotocolFeign {

    @PostMapping(value = "/coinprotocolFeign/list")
    List<CoinprotocolDTO> list();

    @PostMapping(value = "/coinprotocolFeign/findByProtocol")
    Coinprotocol findByProtocol(@RequestParam("protocol")Integer protocol);

    @GetMapping(value = "/coinprotocolFeign/findAll")
    Page<Coinprotocol> findAll(@RequestParam("pageNo")Integer pageNo, @RequestParam("pageSize")Integer pageSize);

    @PostMapping(value = "/coinprotocolFeign/save")
    Coinprotocol save(@RequestBody Coinprotocol coinprotocol);

    @PostMapping(value = "/coinprotocolFeign/findBySymbol")
    Coinprotocol findBySymbol(@RequestParam("symbol")String symbol);
}

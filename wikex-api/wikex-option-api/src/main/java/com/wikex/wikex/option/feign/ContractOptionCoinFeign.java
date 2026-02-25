package com.wikex.wikex.option.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-option",contextId = "contractOptionCoinFeign")
public interface ContractOptionCoinFeign {

    @PostMapping(value = "/coinFeign/findAll")
    Page<ContractOptionCoin> findAll(@RequestBody PageParam pageParam);

    @PostMapping(value = "/coinFeign/findOneBySymbol")
    ContractOptionCoin findOneBySymbol(@RequestParam("symbol") String symbol);

    @PostMapping(value = "/coinFeign/add")
    MessageResult add(@RequestBody ContractOptionCoin contractOptionCoin);

    @PostMapping(value = "/coinFeign/alert")
    MessageResult alert(@RequestBody ContractOptionCoin coin);
}

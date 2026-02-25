package com.wikex.wikex.swap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.constant.ContractOrderType;
import com.wikex.wikex.screen.ContractOrderEntrustScreen;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(value = "wikex-swap",contextId = "contractOrderFeign")
public interface ContractOrderEntrustFeign {

    /**
     * 
     * @param screen
     * @return
     */
    @PostMapping("/orderFeign/page-query")
    Page<ContractOrderEntrust> pageQuery(@RequestBody ContractOrderEntrustScreen screen);

    @GetMapping("/orderFeign/findOne")
    ContractOrderEntrust findOne(@RequestParam("id") Long id);

    @PostMapping("/orderFeign/findAll4Agent")
    Page<ContractOrderEntrust> findAll4Agent(@RequestParam("memberId") Long memberId, @RequestParam("pageParam") PageParam pageParam, @RequestBody ContractOrderEntrustScreen screen);

    @PostMapping("/orderFeign/sendReward")
    void sendReward();

    @PostMapping("/orderFeign/insertOrder")
    MessageResult insertOrder(@RequestParam(value = "memberId") Long memberId,
                              @RequestParam(value = "contractCoinId") Long contractCoinId,
                              @RequestParam(value = "direction") ContractOrderDirection direction,
                              @RequestParam(value = "type") ContractOrderType type,
                              @RequestParam(value = "triggerPrice", required = false) BigDecimal triggerPrice,
                              @RequestParam(value = "entrustPrice") BigDecimal entrustPrice,
                              @RequestParam(value = "leverage") BigDecimal leverage,
                              @RequestParam(value = "principalAmount") BigDecimal principalAmount,
                              @RequestParam(value = "pattern") ContractOrderPattern pattern);
}

package com.wikex.wikex.udun.feign;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "wikexWalletFeign", contextId = "wikexWalletFeign", url = "${wikex.wallet.url}")
public interface WikexWalletFeign {

    @PostMapping("/api/wallet/wallets")
    Map<String, Object> createWallet(@RequestHeader("Access-Key") String accessKey,
            @RequestBody Map<String, Object> request);

    @GetMapping("/api/deposits/deposit-support")
    List<Map<String, Object>> getDepositSupport(@RequestHeader("Access-Key") String accessKey);

}

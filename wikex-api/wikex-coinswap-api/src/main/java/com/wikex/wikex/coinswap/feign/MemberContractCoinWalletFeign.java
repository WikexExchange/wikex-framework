package com.wikex.wikex.coinswap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberContractWalletCoinScreen;
import com.wikex.wikex.screen.MemberContractWalletScreen;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-coin-swap",contextId = "walletCoinFeign")
public interface MemberContractCoinWalletFeign {

    @PostMapping(value = "/walletFeign/initWallet")
    MessageResult initWallet(@RequestParam("contractId")Long contractId);

    @PostMapping(value = "/walletFeign/findAll")
    Page<MemberContractWalletCoin> findAll(@RequestBody MemberContractWalletCoinScreen screen);
}

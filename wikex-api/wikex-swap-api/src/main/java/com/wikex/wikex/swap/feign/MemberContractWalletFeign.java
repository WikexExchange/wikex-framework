package com.wikex.wikex.swap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberContractWalletScreen;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-swap",contextId = "walletFeign")
public interface MemberContractWalletFeign {

    @PostMapping(value = "/walletFeign/initWallet")
    MessageResult initWallet(@RequestParam("contractId")Long contractId);

    @PostMapping(value = "/walletFeign/findAll")
    Page<MemberContractWallet> findAll(@RequestBody MemberContractWalletScreen screen);

    @PostMapping(value = "/walletFeign/findOne")
    MemberContractWallet findOne(@RequestParam("walletId") Long walletId);

//    @PostMapping(value = "/walletFeign/findAllMemberContractWallet")
//    Page<MemberContractWallet> findAllMemberContractWallet(MemberContractWalletScreen screen);
}

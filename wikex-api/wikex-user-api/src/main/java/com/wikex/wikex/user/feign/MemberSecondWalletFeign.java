package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.ContractFinanceScreen;
import com.wikex.wikex.user.entity.MemberSecondWallet;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-user",contextId = "memberSecondWalletFeign")
public interface MemberSecondWalletFeign {

    @PostMapping("/memberSecondWalletFeign/findAllByMemberId")
    List<MemberSecondWallet> findAllByMemberId(@RequestParam("memberId") Long memberId);

    @PostMapping("/memberSecondWalletFeign/save")
    MessageResult save(@RequestBody MemberSecondWallet wallet);

    @PostMapping("/memberSecondWalletFeign/findByCoinUnitAndMemberId")
    MemberSecondWallet findByCoinUnitAndMemberId(@RequestParam("unit") String unit, @RequestParam("memberId") Long memberId);

    @PostMapping("/memberSecondWalletFeign/increaseBalance")
    MessageResult increaseBalance(@RequestParam("walletId") Long walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberSecondWalletFeign/decreaseBalance")
    MessageResult decreaseBalance(@RequestParam("walletId") Long walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberSecondWalletFeign/decreaseFrozen")
    MessageResult decreaseFrozen(@RequestParam("walletId") Long walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberSecondWalletFeign/thawBalance")
    MessageResult thawBalance(@RequestParam("unit") String unit, @RequestParam("memberId") Long memberId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberSecondWalletFeign/freezeBalance")
    MessageResult freezeBalance(@RequestParam("walletId") Long walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberSecondWalletFeign/findAll")
    Page<MemberSecondWallet> findAll(@RequestBody ContractFinanceScreen screen);
}

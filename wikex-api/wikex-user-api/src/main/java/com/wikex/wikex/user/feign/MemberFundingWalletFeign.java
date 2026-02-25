package com.wikex.wikex.user.feign;

import com.wikex.wikex.user.entity.MemberDeposit;
import com.wikex.wikex.user.entity.MemberFundingWallet;
import com.wikex.wikex.util.MessageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(value = "wikex-user", contextId = "memberFundingWalletFeign")
public interface MemberFundingWalletFeign {

    @PostMapping("/memberFundingWalletFeign/findAllByMemberId")
    List<MemberFundingWallet> findAllByMemberId(@RequestParam("memberId") Long memberId);

    @PostMapping("/memberFundingWalletFeign/save")
    MessageResult save(@RequestBody MemberFundingWallet wallet);

    @PostMapping("/memberFundingWalletFeign/findByCoinUnitAndMemberId")
    MemberFundingWallet findByCoinUnitAndMemberId(@RequestParam("unit") String unit,
            @RequestParam("memberId") Long memberId);

    @PostMapping("/memberFundingWalletFeign/increaseBalance")
    MessageResult increaseBalance(@RequestParam("walletId") Long walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberFundingWalletFeign/decreaseBalance")
    MessageResult decreaseBalance(@RequestParam("walletId") Long walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberFundingWalletFeign/increaseFrozen")
    MessageResult increaseFrozen(@RequestParam("walletId") Long walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberFundingWalletFeign/decreaseFrozen")
    MessageResult decreaseFrozen(@RequestParam("walletId") Long walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberFundingWalletFeign/freezeBalance")
    MessageResult freezeBalance(@RequestParam("id") Long id, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberFundingWalletFeign/thawBalance")
    MessageResult thawBalance(@RequestParam("walletId") Long walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/memberFundingWalletFeign/findDeposit")
    MemberDeposit findDeposit(@RequestParam("address") String address, @RequestParam("txid") String txid);

    @PostMapping("/memberFundingWalletFeign/recharge")
    MessageResult recharge(@RequestParam("unit") String unit, @RequestParam("address") String address,
            @RequestParam("amount") BigDecimal amount, @RequestParam("txid") String txid,
            @RequestParam("fromAddress") String fromAddress, @RequestParam("blockHeight") Long blockHeight);
}

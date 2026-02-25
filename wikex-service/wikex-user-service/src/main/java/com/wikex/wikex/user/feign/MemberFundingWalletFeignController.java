package com.wikex.wikex.user.feign;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.MemberDeposit;
import com.wikex.wikex.user.entity.MemberFundingWallet;
import com.wikex.wikex.user.service.MemberDepositService;
import com.wikex.wikex.user.service.MemberFundingWalletService;
import com.wikex.wikex.util.MessageResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/memberFundingWalletFeign")
public class MemberFundingWalletFeignController extends BaseController {

    @Autowired
    private MemberFundingWalletService memberFundingWalletService;
    @Autowired
    private MemberDepositService memberDepositService;

    @PostMapping("findAllByMemberId")
    public List<MemberFundingWallet> findAllByMemberId(@RequestParam("memberId") Long memberId) {
        return memberFundingWalletService.findAllByMemberId(memberId);
    }

    @PostMapping("save")
    public MessageResult save(@RequestBody MemberFundingWallet wallet) {
        boolean ret = memberFundingWalletService.saveOrUpdate(wallet);
        if (ret) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("findByCoinUnitAndMemberId")
    public MemberFundingWallet findByCoinUnitAndMemberId(@RequestParam("unit") String unit,
            @RequestParam("memberId") Long memberId) {
        return memberFundingWalletService.findByCoinUnitAndMemberId(unit, memberId);
    }

    @PostMapping("increaseBalance")
    public MessageResult increaseBalance(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberFundingWalletService.increaseBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("decreaseBalance")
    public MessageResult decreaseBalance(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberFundingWalletService.decreaseBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("decreaseFrozen")
    public MessageResult decreaseFrozen(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberFundingWalletService.decreaseFrozen(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("increaseFrozen")
    public MessageResult increaseFrozen(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberFundingWalletService.increaseFrozen(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("freezeBalance")
    public MessageResult freezeBalance(@RequestParam("id") Long id, @RequestParam("amount") BigDecimal amount) {
        int ret = memberFundingWalletService.freezeBalance(id, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("thawBalance")
    public MessageResult thawBalance(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberFundingWalletService.thawBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    // @PostMapping("findDeposit")
    // public MemberDeposit findDeposit(@RequestParam("address") String address,
    // @RequestParam("txid") String txid) {
    // return memberDepositService.findDeposit(address, txid);
    // }

    @PostMapping("recharge")
    public MessageResult recharge(@RequestParam("unit") String unit,
            @RequestParam("address") String address,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("txid") String txid,
            @RequestParam("fromAddress") String fromAddress,
            @RequestParam("blockHeight") Long blockHeight) {
        return memberFundingWalletService.recharge(unit, address, amount, txid, fromAddress, blockHeight);
    }
}

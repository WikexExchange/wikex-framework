package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.ContractFinanceScreen;
import com.wikex.wikex.user.entity.MemberSecondWallet;
import com.wikex.wikex.user.service.MemberSecondWalletService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/memberSecondWalletFeign")
public class MemberSecondWalletFeignController extends BaseController {

    @Autowired
    private MemberSecondWalletService memberSecondWalletService;
    @Autowired
    private MemberService memberService;

    @PostMapping("findAllByMemberId")
    public List<MemberSecondWallet> findAllByMemberId(@RequestParam("memberId") Long memberId) {
        List<MemberSecondWallet> memberSecondWallet = memberSecondWalletService.findAllByMemberId(memberId);
        return memberSecondWallet;
    }

    @PostMapping("save")
    public MessageResult save(@RequestBody MemberSecondWallet memberSecondWallet) {
        boolean ret = memberSecondWalletService.saveOrUpdate(memberSecondWallet);
        if (ret) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("findByCoinUnitAndMemberId")
    public MemberSecondWallet findByCoinUnitAndMemberId(@RequestParam("unit") String unit,
            @RequestParam("memberId") Long memberId) {
        MemberSecondWallet memberSecondWallet = memberSecondWalletService.findByCoinUnitAndMemberId(unit, memberId);
        return memberSecondWallet;
    }

    @PostMapping("increaseBalance")
    public MessageResult increaseBalance(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberSecondWalletService.increaseBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("decreaseBalance")
    public MessageResult decreaseBalance(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberSecondWalletService.decreaseBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("decreaseFrozen")
    public MessageResult decreaseFrozen(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberSecondWalletService.decreaseFrozen(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("thawBalance")
    public MessageResult thawBalance(@RequestParam("unit") String unit, @RequestParam("memberId") Long memberId,
            @RequestParam("amount") BigDecimal amount) {
        MemberSecondWallet memberSecondWallet = memberSecondWalletService.findByCoinUnitAndMemberId(unit, memberId);
        if (memberSecondWallet == null) {
            return MessageResult.error("Information Expired");
        }
        int ret = memberSecondWalletService.thawBalance(memberSecondWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("freezeBalance")
    public MessageResult freezeBalance(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberSecondWalletService.freezeBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("findAll")
    public Page<MemberSecondWallet> findAll(@RequestBody ContractFinanceScreen screen) {
        return memberSecondWalletService.findAll(screen);
    }
}

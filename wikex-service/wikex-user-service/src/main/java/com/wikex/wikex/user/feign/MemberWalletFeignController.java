package com.wikex.wikex.user.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.screen.MemberWalletScreen;
import com.wikex.wikex.user.dto.MemberWalletDTO;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/memberWalletFeign")
public class MemberWalletFeignController extends BaseController {

    @Autowired
    private MemberWalletService memberWalletService;

    @GetMapping("findByCoinUnitAndMemberId")
    public MemberWallet findByCoinUnitAndMemberId(@RequestParam("coinUnit") String coinUnit,
            @RequestParam("memberId") Long memberId) {
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(coinUnit, memberId);
        return memberWallet;
    }

    @PostMapping("freezeBalance")
    public MessageResult freezeBalance(@RequestParam("id") Long id, @RequestParam("amount") BigDecimal amount) {
        int ret = memberWalletService.freezeBalance(id, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("deductBalance")
    public MessageResult deductBalance(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberWalletService.decreaseBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("increaseBalance")
    public MessageResult increaseBalance(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberWalletService.increaseBalance(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("increaseToRelease")
    MessageResult increaseToRelease(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberWalletService.increaseToRelease(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("decreaseToRelease")
    MessageResult decreaseToRelease(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberWalletService.decreaseToRelease(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("decreaseFrozen")
    public MessageResult decreaseFrozen(@RequestParam("walletId") Long walletId,
            @RequestParam("amount") BigDecimal amount) {
        int ret = memberWalletService.decreaseFrozen(walletId, amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @PostMapping("thawBalance")
    public MessageResult thawBalance(@RequestParam("coinId") String coinId, @RequestParam("memberId") Long memberId,
            @RequestParam("amount") BigDecimal amount) {
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(coinId, memberId);
        if (memberWallet == null) {
            return MessageResult.error("Information Expired");
        }
        int ret = memberWalletService.thawBalance(memberWallet.getId(), amount);
        if (ret > 0) {
            return MessageResult.success();
        } else {
            return MessageResult.error("Information Expired");
        }
    }

    @GetMapping("findAllByMemberId")
    public List<MemberWallet> findAllByMemberId(@RequestParam("memberId") Long memberId) {
        List<MemberWallet> list = memberWalletService.findAllByMemberId(memberId);
        return list;
    }

    @PostMapping(value = "/save")
    public Boolean save(@RequestBody MemberWallet wallet) {
        return memberWalletService.saveOrUpdate(wallet);
    }

    @PostMapping(value = "/getBalance")
    public Page<MemberWalletDTO> getBalance(@RequestBody MemberWalletScreen screen) {
        return memberWalletService.getBalance(screen);
    }

    @PostMapping("/lockWallet")
    public boolean lockWallet(@RequestParam("uid") Long uid, @RequestParam("unit") String unit) {
        return memberWalletService.lockWallet(uid, unit);
    }

    @PostMapping("/unlockWallet")
    boolean unlockWallet(@RequestParam("uid") Long uid, @RequestParam("unit") String unit) {
        return memberWalletService.unlockWallet(uid, unit);
    }
}

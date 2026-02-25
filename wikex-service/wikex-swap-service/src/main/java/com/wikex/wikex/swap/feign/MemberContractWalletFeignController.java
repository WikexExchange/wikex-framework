package com.wikex.wikex.swap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.screen.MemberContractWalletScreen;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.MemberContractWalletService;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/walletFeign")
public class MemberContractWalletFeignController {
    @Autowired
    private MemberContractWalletService memberContractWalletService;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ContractCoinService contractCoinService;








    @RequestMapping("initWallet")
    public MessageResult initWallet(@RequestParam("contractId")Long contractId) {
        List<Member> list = memberFeign.findAllList();
        ContractCoin coin = contractCoinService.getById(contractId);

        List<MemberContractWallet> walletList = new ArrayList<>();
        for (Member member : list) {
            List<MemberContractWallet> wlist = memberContractWalletService.findAllByMemberId(member.getId());
            
            if(wlist == null || wlist.size()==0) {
                
                MemberContractWallet wallet = createMemberContractWallet(member, coin);
                walletList.add(wallet);
            }
        }
        if(walletList.size()>0){
            memberContractWalletService.saveBatch(walletList);
        }
        return MessageResult.success();
    }

    private MemberContractWallet createMemberContractWallet(Member member, ContractCoin coin) {
        MemberContractWallet wallet = new MemberContractWallet();
        wallet.setUsdtPattern(ContractOrderPattern.CROSSED);
        wallet.setUsdtBuyPosition(BigDecimal.ZERO);
        wallet.setUsdtTotalProfitAndLoss(BigDecimal.ZERO);
        wallet.setCoinTotalProfitAndLoss(BigDecimal.ZERO);
        wallet.setMemberId(member.getId());
        wallet.setUsdtBalance(BigDecimal.ZERO);
        wallet.setUsdtBuyLeverage(BigDecimal.TEN);
        wallet.setUsdtBuyPrice(BigDecimal.ZERO);
        wallet.setUsdtBuyPrincipalAmount(BigDecimal.ZERO);
        wallet.setUsdtFrozenBalance(BigDecimal.ZERO);
        wallet.setUsdtFrozenBuyPosition(BigDecimal.ZERO);
        wallet.setUsdtFrozenSellPosition(BigDecimal.ZERO);
        wallet.setUsdtSellLeverage(BigDecimal.TEN);
        wallet.setUsdtSellPosition(BigDecimal.ZERO);
        wallet.setUsdtSellPrice(BigDecimal.ZERO);
        wallet.setUsdtSellPrincipalAmount(BigDecimal.ZERO);
        wallet.setUsdtShareNumber(coin.getShareNumber());
        return wallet;
    }


    @RequestMapping("findAll")
    public Page<MemberContractWallet> findAll(@RequestBody MemberContractWalletScreen screen) {
        return memberContractWalletService.findAll(screen);
    }





    @RequestMapping("findOne")
    public MemberContractWallet findOne(@RequestParam("walletId")Long walletId) {
        return memberContractWalletService.getById(walletId);
    }

}

package com.wikex.wikex.coinswap.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.MemberContractWalletCoinService;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.screen.MemberContractWalletCoinScreen;
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
    private MemberContractWalletCoinService memberContractWalletService;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private ContractCoinCoinService contractCoinService;


    @RequestMapping("initWallet")
    public MessageResult initWallet(@RequestParam("contractId")Long contractId) {
        List<Member> list = memberFeign.findAllList();
        ContractCoinCoin coin = contractCoinService.getById(contractId);
        List<MemberContractWalletCoin> walletList = new ArrayList<>();
        for (Member member : list) {
            MemberContractWalletCoin wallet1 = memberContractWalletService.findByMemberIdAndContractCoin(member.getId(), coin);
            if(wallet1==null || wallet1.getId()==null){
                MemberContractWalletCoin wallet = createMemberContractWallet(member, coin);
                walletList.add(wallet);
            }
        }
        if(walletList.size()>0){
            memberContractWalletService.saveBatch(walletList);
        }
        return MessageResult.success();
    }

    @RequestMapping("findAll")
    public Page<MemberContractWalletCoin> findAll(@RequestBody MemberContractWalletCoinScreen screen) {
        return memberContractWalletService.findAll(screen);
    }

    private MemberContractWalletCoin createMemberContractWallet(Member member, ContractCoinCoin coin) {
        MemberContractWalletCoin wallet = new MemberContractWalletCoin();
        wallet.setCoinPattern(ContractOrderPattern.FIXED);
        wallet.setCoinBuyPosition(BigDecimal.ZERO);
        wallet.setCoinTotalProfitAndLoss(BigDecimal.ZERO);
        wallet.setCoinBalance(BigDecimal.ZERO);
        wallet.setCoinBuyLeverage(BigDecimal.TEN); 
        wallet.setCoinBuyPosition(BigDecimal.ZERO);
        wallet.setCoinBuyPrice(BigDecimal.ZERO);
        wallet.setCoinBuyPrincipalAmount(BigDecimal.ZERO);
        wallet.setCoinFrozenBalance(BigDecimal.ZERO);
        wallet.setCoinFrozenBuyPosition(BigDecimal.ZERO);
        wallet.setCoinFrozenSellPosition(BigDecimal.ZERO);
        wallet.setCoinPattern(ContractOrderPattern.FIXED);
        wallet.setCoinSellLeverage(BigDecimal.TEN);
        wallet.setCoinSellPosition(BigDecimal.ZERO);
        wallet.setCoinSellPrice(BigDecimal.ZERO);
        wallet.setCoinSellPrincipalAmount(BigDecimal.ZERO);
        wallet.setCoinShareNumber(coin.getShareNumber());
        wallet.setCoinTotalProfitAndLoss(BigDecimal.ZERO);
        wallet.setContractCoin(coin);
        wallet.setContractId(coin.getId());
        wallet.setMemberId(member.getId());
        wallet.setCoinBalance(BigDecimal.ZERO);
        wallet.setCoinBuyLeverage(BigDecimal.TEN);
        wallet.setCoinBuyPrice(BigDecimal.ZERO);
        wallet.setCoinBuyPrincipalAmount(BigDecimal.ZERO);
        wallet.setCoinFrozenBalance(BigDecimal.ZERO);
        wallet.setCoinFrozenBuyPosition(BigDecimal.ZERO);
        wallet.setCoinFrozenSellPosition(BigDecimal.ZERO);
        wallet.setCoinSellLeverage(BigDecimal.TEN);
        wallet.setCoinSellPosition(BigDecimal.ZERO);
        wallet.setCoinSellPrice(BigDecimal.ZERO);
        wallet.setCoinSellPrincipalAmount(BigDecimal.ZERO);
        wallet.setCoinShareNumber(coin.getShareNumber());
        return wallet;
    }

}

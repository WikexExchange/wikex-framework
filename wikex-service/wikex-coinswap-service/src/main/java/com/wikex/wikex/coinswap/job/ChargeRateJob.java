package com.wikex.wikex.coinswap.job;


import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.coinswap.engine.ContractCoinMatch;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.MemberContractWalletCoinService;
import com.wikex.wikex.coinswap.vo.ChargeRateVo;
import com.wikex.wikex.coinswap.vo.ResponseChargeRate;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.ProxyUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ChargeRateJob {


    @Autowired
    private ContractCoinMatchFactory matchFactory;
    @Autowired
    private ContractCoinCoinService contractCoinService;
    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private MemberContractWalletCoinService memberContractWalletService;

    private RestTemplate restTemplate = new RestTemplate();

    @XxlJob("updateChargeRate")
    public void updateChargeRate(){
        //        String url = "https://api.hbdm.com//linear-swap-api/v1/swap_batch_funding_rate";
        String url = "https://api.hbdm.com/swap-api/v1/swap_batch_funding_rate";
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(ProxyUtil.getProxy());
        restTemplate.setRequestFactory(requestFactory);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Map<String,BigDecimal> rateMap = new HashMap<>();
        if(response.getStatusCodeValue()==200){
            String body = response.getBody();
            if(StringUtils.isNotEmpty(body)){
                ResponseChargeRate responseChargeRate = JSON.parseObject(body, ResponseChargeRate.class);
                if("ok".equals(responseChargeRate.getStatus())){
                    List<ChargeRateVo> data = responseChargeRate.getData();
                    for (ChargeRateVo datum : data) {
                        rateMap.put(datum.getContract_code()+"T",datum.getFunding_rate()!=null?new BigDecimal(datum.getFunding_rate()):BigDecimal.ZERO);
                    }
                }
            }
        }
        List<ContractCoinCoin> coinCoins = contractCoinService.list();
        for (ContractCoinCoin coinCoin : coinCoins) {
            coinCoin.setFeePercent(rateMap.get(coinCoin.getSymbol().replaceAll("/","-")));
            contractCoinService.saveOrUpdate(coinCoin);
        }

    }


//    @Scheduled(cron = "0 1 0,8,16 * * ? ")
    @XxlJob("handleChargeRate")
    public void handleChargeRate(){
        Map<String, ContractCoinMatch> matchMap = matchFactory.getMatchMap();
        for (String symbol : matchMap.keySet()) {
            ContractCoinMatch match = matchMap.get(symbol);
            ContractCoinCoin coin = contractCoinService.findBySymbol(symbol);
            BigDecimal feePercent = coin.getFeePercent();
            List<MemberContractWalletCoin> buyWallets = memberContractWalletService.getWalletsByBuyPosition(coin.getId());
            List<MemberContractWalletCoin> sellWallets = memberContractWalletService.getWalletsBySellPosition(coin.getId());

            if(feePercent.compareTo(BigDecimal.ZERO)>0){
                
                if(buyWallets!=null && buyWallets.size()>0 && sellWallets!=null && sellWallets.size()>0) {
                    BigDecimal totalFee = BigDecimal.ZERO;
                    for (MemberContractWalletCoin wallet : buyWallets) {
                        BigDecimal fee = wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(),8, RoundingMode.HALF_UP).multiply(feePercent);
                        memberContractWalletService.justDecreaseCoinBuyPrincipalAmount(wallet.getId(), fee);
                        totalFee = totalFee.add(fee);
                        
                        this.createMemberTransaction(wallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,fee,coin.getCoinSymbol());
                        match.memberWalletChange(wallet.getId());
                    }

                    BigDecimal totalValue = BigDecimal.ZERO;
                    for (MemberContractWalletCoin wallet : sellWallets) {
                        totalValue = totalValue.add(wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinSellPrice(),8, RoundingMode.HALF_UP));
                    }
                    for (MemberContractWalletCoin wallet : sellWallets) {
                        BigDecimal fee = totalFee.multiply(wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinSellPrice(),8, RoundingMode.HALF_UP)).divide(totalValue,8, RoundingMode.HALF_UP);
                        memberContractWalletService.justIncreaseCoinSellPrincipalAmount(wallet.getId(), fee);
                        
                        this.createMemberTransaction(wallet.getMemberId(),TransactionType.GET_CHARGE_FEE,fee,coin.getCoinSymbol());
                        match.memberWalletChange(wallet.getId());
                    }

                }else {
                    
                    if(buyWallets!=null && buyWallets.size()>0){
                        
                        BigDecimal totalFee = BigDecimal.ZERO;
                        for (MemberContractWalletCoin wallet : buyWallets) {
                            BigDecimal fee = wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(),8, RoundingMode.HALF_UP).multiply(feePercent);
                            memberContractWalletService.justDecreaseCoinBuyPrincipalAmount(wallet.getId(), fee);
                            totalFee = totalFee.add(fee);
                            
                            this.createMemberTransaction(wallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,fee,coin.getCoinSymbol());
                            match.memberWalletChange(wallet.getId());
                        }
                        
                        MemberContractWalletCoin systemWallet = memberContractWalletService.findByMemberIdAndContractCoin(1L, coin);
                        memberContractWalletService.justIncreaseCoinSellPrincipalAmount(systemWallet.getId(), totalFee);
                        
                        this.createMemberTransaction(systemWallet.getMemberId(),TransactionType.GET_CHARGE_FEE,totalFee,coin.getCoinSymbol());

                    }else if(sellWallets!=null && sellWallets.size()>0){
                        
                        BigDecimal totalFee = BigDecimal.ZERO;
                        for (MemberContractWalletCoin wallet : sellWallets) {
                            BigDecimal fee = wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinSellPrice(),8, RoundingMode.HALF_UP).multiply(feePercent);
                            memberContractWalletService.justIncreaseCoinSellPrincipalAmount(wallet.getId(), fee);
                            totalFee = totalFee.add(fee);
                            
                            this.createMemberTransaction(wallet.getMemberId(),TransactionType.GET_CHARGE_FEE,fee,coin.getCoinSymbol());
                            match.memberWalletChange(wallet.getId());
                        }
                        
                        
                        MemberContractWalletCoin systemWallet = memberContractWalletService.findByMemberIdAndContractCoin(1L, coin);
                        memberContractWalletService.justDecreaseCoinBuyPrincipalAmount(systemWallet.getId(), totalFee);
                        
                        this.createMemberTransaction(systemWallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,totalFee,coin.getCoinSymbol());
                    }
                }

            }else if(feePercent.compareTo(BigDecimal.ZERO)<0){
                feePercent = BigDecimal.ZERO.subtract(feePercent);
                
                
                if(buyWallets!=null && buyWallets.size()>0 && sellWallets!=null && sellWallets.size()>0) {
                    BigDecimal totalFee = BigDecimal.ZERO;
                    for (MemberContractWalletCoin wallet : sellWallets) {
                        BigDecimal fee = wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinSellPrice(),8, RoundingMode.HALF_UP).multiply(feePercent);
                        memberContractWalletService.justDecreaseCoinSellPrincipalAmount(wallet.getId(), fee);
                        totalFee = totalFee.add(fee);
                        
                        this.createMemberTransaction(wallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,fee,coin.getCoinSymbol());
                        match.memberWalletChange(wallet.getId());
                    }

                    BigDecimal totalValue = BigDecimal.ZERO;
                    for (MemberContractWalletCoin wallet : buyWallets) {
                        totalValue = totalValue.add(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(),8, RoundingMode.HALF_UP));
                    }
                    for (MemberContractWalletCoin wallet : buyWallets) {
                        BigDecimal fee = totalFee.multiply(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(),8, RoundingMode.HALF_UP)).divide(totalValue,8, RoundingMode.HALF_UP);
                        memberContractWalletService.justIncreaseCoinBuyPrincipalAmount(wallet.getId(), fee);
                        
                        this.createMemberTransaction(wallet.getMemberId(),TransactionType.GET_CHARGE_FEE,fee,coin.getCoinSymbol());
                        match.memberWalletChange(wallet.getId());
                    }

                }else {
                    
                    if(buyWallets!=null && buyWallets.size()>0){
                        
                        BigDecimal totalFee = BigDecimal.ZERO;
                        for (MemberContractWalletCoin wallet : buyWallets) {
                            BigDecimal fee = wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(),8, RoundingMode.HALF_UP).multiply(feePercent);
                            memberContractWalletService.justIncreaseCoinBuyPrincipalAmount(wallet.getId(), fee);
                            totalFee = totalFee.add(fee);
                            
                            this.createMemberTransaction(wallet.getMemberId(),TransactionType.GET_CHARGE_FEE,fee,coin.getCoinSymbol());
                            match.memberWalletChange(wallet.getId());
                        }
                        
                        MemberContractWalletCoin systemWallet = memberContractWalletService.findByMemberIdAndContractCoin(1L, coin);
                        memberContractWalletService.justDecreaseCoinSellPrincipalAmount(systemWallet.getId(), totalFee);
                        
                        this.createMemberTransaction(systemWallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,totalFee,coin.getCoinSymbol());

                    }else if(sellWallets!=null && sellWallets.size()>0){
                        
                        BigDecimal totalFee = BigDecimal.ZERO;
                        for (MemberContractWalletCoin wallet : sellWallets) {
                            BigDecimal fee = wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()).multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinSellPrice(),8, RoundingMode.HALF_UP).multiply(feePercent);
                            memberContractWalletService.justDecreaseCoinSellPrincipalAmount(wallet.getId(), fee);
                            totalFee = totalFee.add(fee);
                            
                            this.createMemberTransaction(wallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,fee,coin.getCoinSymbol());
                            match.memberWalletChange(wallet.getId());
                        }
                        
                        MemberContractWalletCoin systemWallet = memberContractWalletService.findByMemberIdAndContractCoin(1L, coin);
                        memberContractWalletService.justIncreaseCoinBuyPrincipalAmount(systemWallet.getId(), totalFee);
                        
                        this.createMemberTransaction(systemWallet.getMemberId(),TransactionType.GET_CHARGE_FEE,totalFee,coin.getCoinSymbol());
                    }
                }
            }
        }
    }

    private void createMemberTransaction(Long memberId,TransactionType type,BigDecimal fee,String symbol){
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(fee);
        memberTransaction.setMemberId(memberId);
        memberTransaction.setSymbol(symbol);
        memberTransaction.setType(type.getCode());
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransactionFeign.save(memberTransaction);
    }


}

package com.wikex.wikex.swap.job;


import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.MemberContractWalletService;
import com.wikex.wikex.swap.vo.ChargeRateVo;
import com.wikex.wikex.swap.vo.ResponseChargeRate;
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
    private ContractCoinService contractCoinService;
    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private MemberContractWalletService memberContractWalletService;

    private RestTemplate restTemplate = new RestTemplate();

//    @Scheduled(cron = "30 * * * * ?")
    @XxlJob("updateChargeRate")
    public void updateChargeRate(){
        String url = "https://api.hbdm.com//linear-swap-api/v1/swap_batch_funding_rate"; 
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
                        rateMap.put(datum.getContract_code(),datum.getFunding_rate()!=null?new BigDecimal(datum.getFunding_rate()):BigDecimal.ZERO);
                    }
                }
            }
        }
        List<ContractCoin> coinCoins = contractCoinService.list();
        for (ContractCoin coinCoin : coinCoins) {
            coinCoin.setFeePercent(rateMap.get(coinCoin.getSymbol().replaceAll("/","-")));
            contractCoinService.saveOrUpdate(coinCoin);
        }

    }


    

    @XxlJob("handleChargeRate")
    public void handleChargeRate(){
        Map<String, ContractCoinMatch> matchMap = matchFactory.getMatchMap();
        for (String symbol : matchMap.keySet()) {
            ContractCoinMatch match = matchMap.get(symbol);
            
            ContractCoin coin = contractCoinService.findBySymbol(symbol);
            
            BigDecimal feePercent = coin.getFeePercent();
            
            List<MemberContractWallet> buyWallets = memberContractWalletService.getWalletsByBuyPosition();
            List<MemberContractWallet> sellWallets = memberContractWalletService.getWalletsBySellPosition();

            if(feePercent.compareTo(BigDecimal.ZERO)>0){
                
                
                if(buyWallets!=null && buyWallets.size()>0 && sellWallets!=null && sellWallets.size()>0) {
                    BigDecimal totalFee = BigDecimal.ZERO;
                    for (MemberContractWallet wallet : buyWallets) {
                        BigDecimal fee = wallet.getUsdtBuyPosition().add(wallet.getUsdtFrozenBuyPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtBuyPrice()).multiply(feePercent);
                        memberContractWalletService.justDecreaseUsdtBuyPrincipalAmount(wallet.getId(), fee);
                        totalFee = totalFee.add(fee);
                        
                        this.createMemberTransaction(wallet.getMemberId(), TransactionType.PAY_CHARGE_FEE,fee,"USDT");
                        match.memberWalletChange(wallet.getId());
                    }

                    BigDecimal totalValue = BigDecimal.ZERO;
                    for (MemberContractWallet wallet : sellWallets) {
                        totalValue = totalValue.add(wallet.getUsdtSellPosition().add(wallet.getUsdtFrozenSellPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtSellPrice()));
                    }
                    for (MemberContractWallet wallet : sellWallets) {
                        BigDecimal fee = totalFee.multiply(wallet.getUsdtSellPosition().add(wallet.getUsdtFrozenSellPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtSellPrice())).divide(totalValue,8, RoundingMode.HALF_UP);
                        memberContractWalletService.justIncreaseUsdtSellPrincipalAmount(wallet.getId(), fee);
                        
                        this.createMemberTransaction(wallet.getMemberId(),TransactionType.GET_CHARGE_FEE,fee,"USDT");
                        match.memberWalletChange(wallet.getId());
                    }

                }else {
                    
                    if(buyWallets!=null && buyWallets.size()>0){
                        
                        BigDecimal totalFee = BigDecimal.ZERO;
                        for (MemberContractWallet wallet : buyWallets) {
                            BigDecimal fee = wallet.getUsdtBuyPosition().add(wallet.getUsdtFrozenBuyPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtBuyPrice()).multiply(feePercent);
                            memberContractWalletService.justDecreaseUsdtBuyPrincipalAmount(wallet.getId(), fee);
                            totalFee = totalFee.add(fee);
                            
                            this.createMemberTransaction(wallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,fee,"USDT");
                            match.memberWalletChange(wallet.getId());
                        }
                        
                        MemberContractWallet systemWallet = memberContractWalletService.findByMemberId(1L);
                        memberContractWalletService.justIncreaseUsdtSellPrincipalAmount(systemWallet.getId(), totalFee);
                        
                        this.createMemberTransaction(systemWallet.getMemberId(),TransactionType.GET_CHARGE_FEE,totalFee,"USDT");

                    }else if(sellWallets!=null && sellWallets.size()>0){
                        
                        BigDecimal totalFee = BigDecimal.ZERO;
                        for (MemberContractWallet wallet : sellWallets) {
                            BigDecimal fee = wallet.getUsdtSellPosition().add(wallet.getUsdtFrozenSellPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtSellPrice()).multiply(feePercent);
                            memberContractWalletService.justIncreaseUsdtSellPrincipalAmount(wallet.getId(), fee);
                            totalFee = totalFee.add(fee);
                            
                            this.createMemberTransaction(wallet.getMemberId(),TransactionType.GET_CHARGE_FEE,fee,"USDT");
                            match.memberWalletChange(wallet.getId());
                        }
                        
                        
                        MemberContractWallet systemWallet = memberContractWalletService.findByMemberId(1L);
                        memberContractWalletService.justDecreaseUsdtBuyPrincipalAmount(systemWallet.getId(), totalFee);
                        
                        this.createMemberTransaction(systemWallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,totalFee,"USDT");
                    }
                }

            }else if(feePercent.compareTo(BigDecimal.ZERO)<0){
                feePercent = BigDecimal.ZERO.subtract(feePercent);
                
                
                if(buyWallets!=null && buyWallets.size()>0 && sellWallets!=null && sellWallets.size()>0) {
                    BigDecimal totalFee = BigDecimal.ZERO;
                    for (MemberContractWallet wallet : sellWallets) {
                        BigDecimal fee = wallet.getUsdtSellPosition().add(wallet.getUsdtFrozenSellPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtSellPrice()).multiply(feePercent);
                        memberContractWalletService.justDecreaseUsdtSellPrincipalAmount(wallet.getId(), fee);
                        totalFee = totalFee.add(fee);
                        
                        this.createMemberTransaction(wallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,fee,"USDT");
                        match.memberWalletChange(wallet.getId());
                    }

                    BigDecimal totalValue = BigDecimal.ZERO;
                    for (MemberContractWallet wallet : buyWallets) {
                        totalValue = totalValue.add(wallet.getUsdtBuyPosition().add(wallet.getUsdtFrozenBuyPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtBuyPrice()));
                    }
                    for (MemberContractWallet wallet : buyWallets) {
                        BigDecimal fee = totalFee.multiply(wallet.getUsdtBuyPosition().add(wallet.getUsdtFrozenBuyPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtBuyPrice())).divide(totalValue,8, RoundingMode.HALF_UP);
                        memberContractWalletService.justIncreaseUsdtBuyPrincipalAmount(wallet.getId(), fee);
                        
                        this.createMemberTransaction(wallet.getMemberId(),TransactionType.GET_CHARGE_FEE,fee,"USDT");
                        match.memberWalletChange(wallet.getId());
                    }

                }else {
                    
                    if(buyWallets!=null && buyWallets.size()>0){
                        
                        BigDecimal totalFee = BigDecimal.ZERO;
                        for (MemberContractWallet wallet : buyWallets) {
                            BigDecimal fee = wallet.getUsdtBuyPosition().add(wallet.getUsdtFrozenBuyPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtBuyPrice()).multiply(feePercent);
                            memberContractWalletService.justIncreaseUsdtBuyPrincipalAmount(wallet.getId(), fee);
                            totalFee = totalFee.add(fee);
                            
                            this.createMemberTransaction(wallet.getMemberId(),TransactionType.GET_CHARGE_FEE,fee,"USDT");
                            match.memberWalletChange(wallet.getId());
                        }
                        
                        MemberContractWallet systemWallet = memberContractWalletService.findByMemberId(1L);
                        memberContractWalletService.justDecreaseUsdtSellPrincipalAmount(systemWallet.getId(), totalFee);
                        
                        this.createMemberTransaction(systemWallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,totalFee,"USDT");

                    }else if(sellWallets!=null && sellWallets.size()>0){
                        
                        BigDecimal totalFee = BigDecimal.ZERO;
                        for (MemberContractWallet wallet : sellWallets) {
                            BigDecimal fee = wallet.getUsdtSellPosition().add(wallet.getUsdtFrozenSellPosition()).multiply(wallet.getUsdtShareNumber()).multiply(wallet.getUsdtSellPrice()).multiply(feePercent);
                            memberContractWalletService.justDecreaseUsdtSellPrincipalAmount(wallet.getId(), fee);
                            totalFee = totalFee.add(fee);
                            
                            this.createMemberTransaction(wallet.getMemberId(),TransactionType.PAY_CHARGE_FEE,fee,"USDT");
                            match.memberWalletChange(wallet.getId());
                        }
                        
                        MemberContractWallet systemWallet = memberContractWalletService.findByMemberId(1L);
                        memberContractWalletService.justIncreaseUsdtBuyPrincipalAmount(systemWallet.getId(), totalFee);
                        
                        this.createMemberTransaction(systemWallet.getMemberId(),TransactionType.GET_CHARGE_FEE,totalFee,"USDT");
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

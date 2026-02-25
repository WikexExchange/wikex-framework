package com.wikex.wikex.swap.job;


import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.ContractMarketService;
import com.wikex.wikex.swap.service.MemberContractPositionService;
import com.wikex.wikex.swap.service.MemberContractWalletService;
import com.wikex.wikex.swap.vo.BlastNotice;
import com.wikex.wikex.swap.vo.ChargeRateVo;
import com.wikex.wikex.swap.vo.ResponseChargeRate;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.ProxyUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CalculateEquityJob {


    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private MemberContractPositionService memberContractPositionService;
    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private MemberContractWalletService memberContractWalletService;

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${dictionary.blastRate}")
    private BigDecimal rate;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private ContractMarketService marketService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private String memberPLRedisKey = "U_MEMBER_PL_%s";

    
    @XxlJob("calculateEquity")
    public void calculateEquity(){
        
        List<Long> list = memberContractPositionService.queryHoldingPositionMemberIds();
        List<ContractCoin> coins =  contractCoinService.list();
        
        Map<Long,BigDecimal> memberQYMap = new HashMap<>();
        Map<String,List<Poke>> pokesMap = new HashMap<>();
        for (ContractCoin coin : coins) {
            List<Poke> pokes = marketService.findPokeAndRemove(coin.getSymbol(), "job", null);
            if (pokes != null && pokes.size() > 0) {
                pokesMap.put(coin.getSymbol(),pokes);
            }
        }


        
        if(list!=null && list.size()>0){



            for (Long memberId : list) {
                Map<String,BigDecimal> plMap = memberContractWalletService.getTotalProfitAndLossAndPrincipalAmount(memberId, coins,true,pokesMap);
                BigDecimal profitAndLoss = plMap.get("profitAndLoss");
                BigDecimal principalAmount = plMap.get("principalAmount");
                MemberContractWallet wallet = memberContractWalletService.findByMemberId(memberId);
                String key = String.format(memberPLRedisKey,wallet.getId());
                redisTemplate.opsForValue().set(key,JSON.toJSONString(plMap));
                BigDecimal qy = wallet.getUsdtBalance().add(wallet.getUsdtFrozenBalance()).add(principalAmount).add(profitAndLoss);
                BigDecimal canUse = wallet.getUsdtBalance();
                if(wallet.getUsdtPattern().equals(ContractOrderPattern.CROSSED)){
                    canUse = wallet.getUsdtBalance().add(profitAndLoss);
                }
                String riskRate = "--";
                if (wallet.getUsdtPattern().equals(ContractOrderPattern.CROSSED) && principalAmount.compareTo(BigDecimal.ZERO)!=0) {
                    
                    BigDecimal r = qy.divide(principalAmount,8,BigDecimal.ROUND_DOWN);
                    riskRate = r.multiply(BigDecimal.valueOf(100)).setScale(2,BigDecimal.ROUND_DOWN).stripTrailingZeros().toPlainString() + "%";
                    
                    
                    if(r.compareTo(rate)<=0){
                        
                        
                        BlastNotice notice = new BlastNotice();
                        notice.setPlMap(plMap);
                        notice.setMemberId(memberId);
                        rocketMQTemplate.convertAndSend("swap-blast", JSON.toJSONString(notice));
                    }
                }
                Map<String,Object> massgeQY = new HashMap<>();
                massgeQY.put("canUse",canUse);
                massgeQY.put("userQY",qy);
                massgeQY.put("riskRate",riskRate);
                massgeQY.put("memberId",memberId);
                
                messagingTemplate.convertAndSend("/topic/swap/memberQY/" + memberId, JSON.toJSONString(massgeQY));

            }
        }
        
        List<MemberContractWallet> wallets = memberContractWalletService.findByNotInMemberIds(list);
        if(wallets!=null && wallets.size()>0){
            for (MemberContractWallet wallet : wallets) {
                BigDecimal qy = wallet.getUsdtBalance().add(wallet.getUsdtFrozenBalance());
                BigDecimal canUse = wallet.getUsdtBalance();
                String riskRate = "--";



                Map<String,Object> massgeQY = new HashMap<>();
                massgeQY.put("canUse",canUse);
                massgeQY.put("userQY",qy);
                massgeQY.put("riskRate",riskRate);
                massgeQY.put("memberId",wallet.getMemberId());
                
                messagingTemplate.convertAndSend("/topic/swap/memberQY/" + wallet.getMemberId(), JSON.toJSONString(massgeQY));
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

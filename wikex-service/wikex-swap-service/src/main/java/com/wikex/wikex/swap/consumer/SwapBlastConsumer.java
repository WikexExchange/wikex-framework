package com.wikex.wikex.swap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.wikex.wikex.config.SnowflakeConfig;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.wikex.wikex.swap.entity.MemberContractPosition;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.ContractOrderEntrustService;
import com.wikex.wikex.swap.service.MemberContractPositionService;
import com.wikex.wikex.swap.service.MemberContractWalletService;
import com.wikex.wikex.swap.vo.BlastNotice;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RocketMQMessageListener(topic = "swap-blast", consumerGroup = "swap-blast")
public class SwapBlastConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;
    @Autowired
    private MemberContractPositionService memberContractPositionService;
    @Autowired
    private MemberContractWalletService memberContractWalletService;
    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private SnowflakeConfig snowflakeConfig;
    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private ContractOrderEntrustService contractOrderEntrustService;


    @Override
    public void onMessage(String content) {
        
        if (StringUtils.isEmpty(content)) {
            return;
        }
        BlastNotice notice = JSON.parseObject(content, BlastNotice.class);
        if (notice == null) {
            
            return;
        }
        Map<String, BigDecimal> plMap = notice.getPlMap();
        
        List<MemberContractPosition> list = memberContractPositionService.queryAllHoldingPositions(notice.getMemberId());
        if (list == null || list.size() == 0) {
            
            return;
        }
        MemberContractWallet wallet = memberContractWalletService.findByMemberId(notice.getMemberId());
        Map<Long, ContractCoin> contractCoinMap = new HashMap<>();
        BigDecimal closeFee = BigDecimal.ZERO;
        for (MemberContractPosition position : list) {
            ContractCoin contractCoin = contractCoinMap.get(position.getContractId());
            if (contractCoin == null) {
                contractCoin = contractCoinService.getById(position.getContractId());
                contractCoinMap.put(position.getContractId(), contractCoin);
            }
            BigDecimal fee = null;
            if (position.getDirection() == ContractOrderDirection.BUY) {
                 fee = blastBuy(wallet, position, plMap.get(contractCoin.getSymbol()), contractCoin);
            } else {
                 fee = blastSell(wallet, position, plMap.get(contractCoin.getSymbol()), contractCoin);
            }
            closeFee = closeFee.add(fee);
        }
        for (ContractCoin contractCoin : contractCoinMap.values()) {
            ContractCoinMatch contractCoinMatch = contractCoinMatchFactory.getContractCoinMatch(contractCoin.getSymbol());
            
            List<ContractOrderEntrust> closingList = contractOrderEntrustService.queryAllEntrustClosingOrdersByContractCoin(wallet.getMemberId(), contractCoin.getId(), ContractOrderDirection.SELL);
            for (ContractOrderEntrust item : closingList) {
                contractCoinMatch.cancelContractOrderEntrust(item, true);
            }
            closingList = contractOrderEntrustService.queryAllEntrustClosingOrdersByContractCoin(wallet.getMemberId(), contractCoin.getId(), ContractOrderDirection.BUY);
            for (ContractOrderEntrust item : closingList) {
                contractCoinMatch.cancelContractOrderEntrust(item, true);
            }
        }
        

        BigDecimal profitAndLoss =plMap.get("profitAndLoss");
        BigDecimal principalAmount =plMap.get("principalAmount");

        
        BigDecimal balance = wallet.getUsdtBalance().add(wallet.getUsdtFrozenBalance()).add(principalAmount).add(profitAndLoss).subtract(closeFee);
        if(balance.compareTo(BigDecimal.ZERO)>0){
            

            wallet.setUsdtBalance(balance);
            
        }else if(balance.compareTo(BigDecimal.ZERO)<0){
            
            handleMemberTransaction(wallet.getMemberId(), balance,"USDT",TransactionType.CONTRACT_BLAST_LOSS);
            wallet.setUsdtBalance(BigDecimal.ZERO);
            
        }

        
        wallet.setUsdtFrozenBalance(BigDecimal.ZERO);
        memberContractWalletService.updateById(wallet);
    }

    public BigDecimal blastBuy(MemberContractWallet wallet, MemberContractPosition position, BigDecimal price, ContractCoin contractCoin) {
        
        
        BigDecimal num = position.getPrincipalAmount().multiply(position.getLeverage()).divide(position.getPrice(), 8, BigDecimal.ROUND_DOWN);
        BigDecimal buyPL = price.subtract(position.getPrice()).multiply(num);

        BigDecimal closeFee = position.getPrincipalAmount().multiply(position.getLeverage()).multiply(contractCoin.getCloseFee());
        
        ContractOrderEntrust orderEntrust = new ContractOrderEntrust();
        orderEntrust.setContractId(contractCoin.getId()); 
        orderEntrust.setMemberId(wallet.getMemberId()); 
        orderEntrust.setSymbol(contractCoin.getSymbol()); 
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); 
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); 
        orderEntrust.setDirection(ContractOrderDirection.SELL); 
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));


        orderEntrust.setTradedPrice(price); 
        orderEntrust.setPrincipalUnit("USDT"); 
        orderEntrust.setPrincipalAmount(position.getPrincipalAmount()); 
        orderEntrust.setCreateTime(DateUtil.getTimeMillis()); 
        orderEntrust.setType(ContractOrderType.MARKET_PRICE);
        orderEntrust.setTriggerPrice(BigDecimal.ZERO); 
        orderEntrust.setEntrustPrice(BigDecimal.ZERO); 
        orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE); 
        orderEntrust.setTriggeringTime(0L); 
        orderEntrust.setShareNumber(position.getShareNumber());
        orderEntrust.setProfitAndLoss(buyPL); 
        orderEntrust.setPatterns(ContractOrderPattern.creator(position.getPattern())); 
        orderEntrust.setCloseFee(closeFee);
        orderEntrust.setCurrentPrice(price);
        orderEntrust.setIsBlast(1); 
        orderEntrust.setPositionId(position.getId());
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS); 
        boolean retObj = contractOrderEntrustService.save(orderEntrust);
        if (retObj) {
            
            
            contractCoinService.increaseTotalProfit(contractCoin.getId(), BigDecimal.ZERO.subtract(buyPL));
            
            handleMemberTransaction(wallet.getMemberId(), buyPL,contractCoin.getSymbol().split("/")[1],buyPL.compareTo(BigDecimal.ZERO) > 0 ? TransactionType.CONTRACT_PROFIT : TransactionType.CONTRACT_LOSS);
            
            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);
            
            handleMemberTransaction(wallet.getMemberId(), closeFee,contractCoin.getSymbol().split("/")[1],TransactionType.CONTRACT_FEE);
            
            position.setPrincipalAmount(BigDecimal.ZERO);
            position.setFrozenPrincipalAmount(BigDecimal.ZERO);
            position.setProfit(buyPL);
            memberContractPositionService.updateById(position);
        }
        return closeFee;
    }

    
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal blastSell(MemberContractWallet wallet, MemberContractPosition position, BigDecimal price, ContractCoin contractCoin) {
        
        BigDecimal num = position.getPrincipalAmount().multiply(position.getLeverage()).divide(position.getPrice(), 8, BigDecimal.ROUND_DOWN);
        BigDecimal sellPL = position.getPrice().subtract(price).multiply(num);
        BigDecimal closeFee = position.getPrincipalAmount().multiply(position.getLeverage()).multiply(contractCoin.getCloseFee());
        
        ContractOrderEntrust orderEntrust = new ContractOrderEntrust();
        orderEntrust.setContractId(contractCoin.getId()); 
        orderEntrust.setMemberId(wallet.getMemberId()); 
        orderEntrust.setSymbol(contractCoin.getSymbol()); 
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); 
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); 
        orderEntrust.setDirection(ContractOrderDirection.BUY); 
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));


        orderEntrust.setTradedPrice(price); 
        orderEntrust.setPrincipalUnit("USDT"); 
        orderEntrust.setPrincipalAmount(position.getPrincipalAmount()); 
        orderEntrust.setCreateTime(DateUtil.getTimeMillis()); 
        orderEntrust.setType(ContractOrderType.MARKET_PRICE);
        orderEntrust.setTriggerPrice(BigDecimal.ZERO); 
        orderEntrust.setEntrustPrice(BigDecimal.ZERO); 
        orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE); 
        orderEntrust.setTriggeringTime(0L); 
        orderEntrust.setShareNumber(position.getShareNumber());
        orderEntrust.setProfitAndLoss(sellPL); 
        orderEntrust.setPatterns(ContractOrderPattern.creator(position.getPattern())); 
        orderEntrust.setCloseFee(closeFee);
        orderEntrust.setCurrentPrice(price);
        orderEntrust.setIsBlast(1); 
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS); 
        boolean retObj = contractOrderEntrustService.save(orderEntrust);
        if (retObj) {
            
            
            contractCoinService.increaseTotalProfit(contractCoin.getId(), BigDecimal.ZERO.subtract(sellPL));
            
            handleMemberTransaction(wallet.getMemberId(), sellPL,contractCoin.getSymbol().split("/")[1],sellPL.compareTo(BigDecimal.ZERO) > 0 ? TransactionType.CONTRACT_PROFIT : TransactionType.CONTRACT_LOSS);
            
            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);
            
            handleMemberTransaction(wallet.getMemberId(), closeFee,contractCoin.getSymbol().split("/")[1],TransactionType.CONTRACT_FEE);
            
            position.setPrincipalAmount(BigDecimal.ZERO);
            position.setFrozenPrincipalAmount(BigDecimal.ZERO);
            position.setProfit(sellPL);
            memberContractPositionService.updateById(position);
        }
        return closeFee;
    }


    public void handleMemberTransaction(Long memberId, BigDecimal fee,String symbol,TransactionType type){

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

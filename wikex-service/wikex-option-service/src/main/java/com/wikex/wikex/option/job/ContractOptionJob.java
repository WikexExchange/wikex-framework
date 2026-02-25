package com.wikex.wikex.option.job;


import com.wikex.wikex.constant.*;
import com.wikex.wikex.option.engine.ContractOptionCoinMatch;
import com.wikex.wikex.option.engine.ContractOptionCoinMatchFactory;
import com.wikex.wikex.option.entity.ContractOption;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.entity.ContractOptionOrder;
import com.wikex.wikex.option.handler.MongoMarketHandler;
import com.wikex.wikex.option.handler.NettyHandler;
import com.wikex.wikex.option.handler.WebsocketMarketHandler;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.wikex.wikex.option.service.ContractOptionOrderService;
import com.wikex.wikex.option.service.ContractOptionService;
import com.wikex.wikex.option.util.WebSocketConnectionManage;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


@Component
public class ContractOptionJob {

    private Logger logger = LoggerFactory.getLogger(ContractOptionJob.class);

    @Autowired
    private ContractOptionCoinService coinService;

    @Autowired
    private ContractOptionService optionService;

    @Autowired
    private ContractOptionOrderService orderService;

    @Autowired
    private MemberWalletFeign walletService;

    @Autowired
    private MemberTransactionFeign memberTransactionService;

    @Autowired
    private ContractOptionCoinMatchFactory factory;
    @Autowired
    private ExchangePushJob exchangePushJob;

    @Autowired
    MongoMarketHandler mongoMarketHandler;

    @Autowired
    WebsocketMarketHandler wsHandler;

    @Autowired
    NettyHandler nettyHandler;

    protected Random rand = new Random();
    

    @XxlJob("checkOptionCoin")
    public void checkOptionCoin(){
        List<ContractOptionCoin> coinList = coinService.findAll();
        for(int i = 0; i < coinList.size(); i++) {
            if(!factory.containsContractCoinMatch(coinList.get(i).getSymbol())) {
                ContractOptionCoinMatch match = new ContractOptionCoinMatch(coinList.get(i).getSymbol());
                match.addHandler(mongoMarketHandler);
                match.addHandler(wsHandler);
                match.addHandler(nettyHandler);
                match.setExchangePushJob(exchangePushJob);
                match.run();
                factory.addContractCoinMatch(coinList.get(i).getSymbol(), match);

                WebSocketConnectionManage.getWebSocket().subNewCoinPrice(coinList.get(i).getSymbol());
                WebSocketConnectionManage.getWebSocket().subNewCoinDepth(coinList.get(i).getSymbol());
                logger.info("Subscribe to new coin price and depth: " + coinList.get(i).getSymbol());

            }
        }

    }


    @XxlJob("checkOptions")
    public void checkOptions(){
        long currentTime = Calendar.getInstance().getTimeInMillis(); 
        
        List<ContractOptionCoin> coinList = coinService.findAll();
        if(coinList != null) {
            for (int i = 0; i < coinList.size(); i++) {
                
                List<ContractOption> options = optionService.findBySymbolAndStatus(coinList.get(i).getSymbol(), ContractOptionStatus.STARTING);
                for(int j = 0; j < options.size(); j++) {
                    
                    long timeGap = currentTime - options.get(j).getCreateTime();
                    if(timeGap/1000 >= coinList.get(i).getOpenTimeGap() - 3) { 
                        ContractOption temOption = options.get(j);
                        int perOptionNo=temOption.getOptionNo()-1;
                        ContractOption perOption = optionService.findBySymbolAndOptionNo(coinList.get(i).getSymbol(),perOptionNo);
                        BigDecimal perClosePrice=null;
                        if(perOption!=null){
                            perClosePrice = perOption.getPresetPrice();
                        }
                        temOption.setStatus(ContractOptionStatus.OPENING);
                        temOption.setOpenTime(currentTime);
                        temOption.setOpenPrice(perClosePrice==null?factory.getContractCoinMatch(coinList.get(i).getSymbol()).getNowPrice():perClosePrice);
                        optionService.saveOrUpdate(temOption);
                     logger.info("{} - Round {} option contract status changed: Betting => Opening", temOption.getSymbol(), temOption.getOptionNo());


                        List<ContractOption> checkOptions = optionService.findBySymbolAndStatus(coinList.get(i).getSymbol(), ContractOptionStatus.STARTING);
                        
                        if(coinList.get(i).getEnable() == 1 && (checkOptions==null || checkOptions.size()==0)) {
                            ContractOption newOption = new ContractOption();
                            newOption.setResult(ContractOptionResult.WAIT);
                            newOption.setStatus(ContractOptionStatus.STARTING);
                            newOption.setTotalSellCount(0);
                            newOption.setTotalBuyCount(0);
                            newOption.setTotalBuy(BigDecimal.ZERO);
                            newOption.setTotalSell(BigDecimal.ZERO);
                            newOption.setCreateTime(Calendar.getInstance().getTimeInMillis());
                            newOption.setOptionNo(coinList.get(i).getMaxOptionNo() + 1);
                            newOption.setSymbol(coinList.get(i).getSymbol());
                            newOption.setTotalPl(BigDecimal.ZERO);
                            newOption.setInitSell(BigDecimal.ZERO);
                            newOption.setInitBuy(BigDecimal.ZERO);
                            optionService.save(newOption);

                           logger.info("{} - Created new option contract: Round {}", coinList.get(i).getSymbol(), coinList.get(i).getMaxOptionNo() + 1);


                            ContractOptionCoin temCoin = coinList.get(i);
                            temCoin.setMaxOptionNo(temCoin.getMaxOptionNo() + 1);
                            coinService.saveOrUpdate(temCoin);
                        }
                    }else{
                        ContractOption temOption = options.get(j);
                        if(temOption.getStatus() == ContractOptionStatus.STARTING
                            && temOption.getTotalBuy().compareTo(BigDecimal.ZERO) == 0
                            && coinList.get(i).getInitBuyReward().compareTo(BigDecimal.ZERO) > 0) {
                            
                            
                            if(rand.nextInt(100) < 50) {
                                temOption.setInitBuy(coinList.get(i).getInitBuyReward());
                                temOption.setTotalBuy(temOption.getTotalBuy().add(coinList.get(i).getInitBuyReward()));
                                optionService.saveOrUpdate(temOption);
                            }
                        }
                        if(temOption.getStatus() == ContractOptionStatus.STARTING
                                && temOption.getTotalSell().compareTo(BigDecimal.ZERO) == 0
                                && coinList.get(i).getInitSellReward().compareTo(BigDecimal.ZERO) > 0) {
                            
                            
                            if(rand.nextInt(100) < 50) {
                                temOption.setInitSell(coinList.get(i).getInitSellReward());
                                temOption.setTotalSell(temOption.getTotalSell().add(coinList.get(i).getInitSellReward()));
                                optionService.saveOrUpdate(temOption);
                            }
                        }
                    }
                }
                if(options == null || options.size() == 0) {
                    
                    if(coinList.get(i).getEnable() == 1) {
                        ContractOption newOption = new ContractOption();
                        newOption.setResult(ContractOptionResult.WAIT);
                        newOption.setStatus(ContractOptionStatus.STARTING);
                        newOption.setTotalSellCount(0);
                        newOption.setTotalBuyCount(0);
                        newOption.setTotalBuy(BigDecimal.ZERO);
                        newOption.setTotalSell(BigDecimal.ZERO);
                        newOption.setCreateTime(Calendar.getInstance().getTimeInMillis());
                        newOption.setOptionNo(coinList.get(i).getMaxOptionNo() + 1);
                        newOption.setSymbol(coinList.get(i).getSymbol());
                        newOption.setTotalPl(BigDecimal.ZERO);
                        newOption.setInitBuy(BigDecimal.ZERO);
                        newOption.setInitSell(BigDecimal.ZERO);
                        optionService.save(newOption);

                        ContractOptionCoin temCoin = coinList.get(i);
                        temCoin.setMaxOptionNo(temCoin.getMaxOptionNo() + 1);
                        coinService.saveOrUpdate(temCoin);

                      logger.info("{} - Created new option contract: Round {}", temCoin.getSymbol(), temCoin.getMaxOptionNo() + 1);

                    }
                }

                
               logger.info("Checking contracts in OPENING status");
                List<ContractOption> optionsOpening = optionService.findBySymbolAndStatus(coinList.get(i).getSymbol(), ContractOptionStatus.OPENING);
                for(int j = 0; j < optionsOpening.size(); j++) {
                    
                    long timeGap = currentTime - optionsOpening.get(j).getOpenTime();
                    if(timeGap/1000 >= coinList.get(i).getCloseTimeGap() - 3) { 

                        ContractOption temOption = optionsOpening.get(j);
                        if(temOption.getPresetPrice()!=null && temOption.getPresetPrice().doubleValue()!=0){
                            
                            optionService.savePresetPrice(temOption.getSymbol(),temOption.getPresetPrice());
                        }
                        temOption.setStatus(ContractOptionStatus.CLOSED); 
                        temOption.setCloseTime(currentTime);
                        temOption.setClosePrice((temOption.getPresetPrice()==null||temOption.getPresetPrice().doubleValue()==0)?factory.getContractCoinMatch(coinList.get(i).getSymbol()).getNowPrice():temOption.getPresetPrice());

                        logger.info("{} - Round {} option contract status changed: Opening => Closed", temOption.getSymbol(), temOption.getOptionNo());

                        
                        List<ContractOptionOrder> orderList = orderService.findByOptionId(optionsOpening.get(j).getId());
                        
                        if(temOption.getClosePrice().subtract(temOption.getOpenPrice()).compareTo(BigDecimal.ZERO) > 0) {
                             logger.info("{} - Round {} option contract result: Rise", temOption.getSymbol(), temOption.getOptionNo());
                            temOption.setResult(ContractOptionResult.WIN);
                            
                            for(int k = 0; k < orderList.size(); k++) {
                                ContractOptionOrder temOrder = orderList.get(k);
                                MemberWallet wallet = walletService.findByCoinUnitAndMemberId(temOrder.getBaseSymbol(), temOrder.getMemberId());
                                if(orderList.get(k).getDirection() == ContractOptionOrderDirection.BUY) {
                                    
                                    walletService.thawBalance(temOrder.getBaseSymbol(), temOrder.getMemberId(), temOrder.getBetAmount()); 
                                    if(temOrder.getFee().compareTo(BigDecimal.ZERO) > 0) { 
                                        walletService.decreaseFrozen(wallet.getId(), temOrder.getFee());
                                        MemberTransaction memberTransaction = new MemberTransaction();
                                        memberTransaction.setFee(BigDecimal.ZERO);
                                        memberTransaction.setAmount(BigDecimal.ZERO.subtract(temOrder.getFee()));
                                        memberTransaction.setSymbol(temOrder.getBaseSymbol());
                                        memberTransaction.setType(TransactionType.OPTION_FEE.getCode());
                                        memberTransaction.setMemberId(temOrder.getMemberId());
                                        memberTransaction.setRealFee("0");
                                        memberTransaction.setDiscountFee("0");
                                        memberTransaction.setCreateTime(new Date());
                                        memberTransactionService.save(memberTransaction);

                                        
                                        temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getFee()));
                                    }
                                    
                                    BigDecimal reward = BigDecimal.ZERO;



                                    reward = temOrder.getBetAmount().multiply(coinList.get(i).getOods()).setScale(4, RoundingMode.DOWN);
                                    
                                    BigDecimal winFee = reward.multiply(coinList.get(i).getWinFeePercent());
                                    temOrder.setWinFee(winFee);
                                    if(reward.compareTo(BigDecimal.ZERO) > 0) {
                                        
                                        walletService.increaseBalance(wallet.getId(), reward.subtract(winFee));
                                        
                                        MemberTransaction memberTransaction = new MemberTransaction();
                                        memberTransaction.setFee(BigDecimal.ZERO);
                                        memberTransaction.setAmount(reward.subtract(winFee));
                                        memberTransaction.setSymbol(temOrder.getBaseSymbol());
                                        memberTransaction.setType(TransactionType.OPTION_REWARD.getCode());
                                        memberTransaction.setMemberId(temOrder.getMemberId());
                                        memberTransaction.setRealFee("0");
                                        memberTransaction.setDiscountFee("0");
                                        memberTransaction.setCreateTime(new Date());
                                        memberTransactionService.save(memberTransaction);

                                        temOrder.setRewardAmount(reward.subtract(winFee));

                                        
                                        temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getWinFee()));
                                    }

                                    temOrder.setResult(ContractOptionOrderResult.WIN);
                                    temOrder.setStatus(ContractOptionOrderStatus.CLOSE);

                                    orderService.saveOrUpdate(temOrder);
                                }else{
                                    
                                    walletService.decreaseFrozen(wallet.getId(), temOrder.getBetAmount());
                                    MemberTransaction memberTransaction = new MemberTransaction();
                                    memberTransaction.setFee(BigDecimal.ZERO);
                                    memberTransaction.setAmount(BigDecimal.ZERO.subtract(temOrder.getBetAmount()));
                                    memberTransaction.setSymbol(temOrder.getBaseSymbol());
                                    memberTransaction.setType(TransactionType.OPTION_FAIL.getCode());
                                    memberTransaction.setMemberId(temOrder.getMemberId());
                                    memberTransaction.setRealFee("0");
                                    memberTransaction.setDiscountFee("0");
                                    memberTransaction.setCreateTime(new Date());
                                    memberTransactionService.save(memberTransaction);

                                    
                                    temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getBetAmount()));

                                    if(temOrder.getFee().compareTo(BigDecimal.ZERO) > 0) { 
                                        walletService.decreaseFrozen(wallet.getId(), temOrder.getFee());
                                        MemberTransaction memberTransaction1 = new MemberTransaction();
                                        memberTransaction1.setFee(BigDecimal.ZERO);
                                        memberTransaction1.setAmount(BigDecimal.ZERO.subtract(temOrder.getFee()));
                                        memberTransaction1.setSymbol(temOrder.getBaseSymbol());
                                        memberTransaction1.setType(TransactionType.OPTION_FEE.getCode());
                                        memberTransaction1.setMemberId(temOrder.getMemberId());
                                        memberTransaction1.setRealFee("0");
                                        memberTransaction1.setDiscountFee("0");
                                        memberTransaction1.setCreateTime(new Date());
                                        memberTransactionService.save(memberTransaction1);

                                        
                                        temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getFee()));
                                    }

                                    temOrder.setResult(ContractOptionOrderResult.LOSE);
                                    temOrder.setStatus(ContractOptionOrderStatus.CLOSE);

                                    orderService.saveOrUpdate(temOrder);
                                }
                            }
                        }else if(temOption.getClosePrice().subtract(temOption.getOpenPrice()).compareTo(BigDecimal.ZERO) == 0){
                            logger.info("{} - Round {} option contract result: Flat", temOption.getSymbol(), temOption.getOptionNo());

                            temOption.setResult(ContractOptionResult.TIED);
                            
                            if(coinList.get(i).getTiedType() == 1) { 
                                for(int k = 0; k < orderList.size(); k++) {
                                    ContractOptionOrder temOrder = orderList.get(k);

                                    walletService.thawBalance(temOrder.getBaseSymbol(), temOrder.getMemberId(), temOrder.getBetAmount());

                                    if(temOrder.getFee().compareTo(BigDecimal.ZERO) > 0) {
                                        walletService.thawBalance(temOrder.getBaseSymbol(), temOrder.getMemberId(), temOrder.getFee());
                                    }

                                    temOrder.setResult(ContractOptionOrderResult.TIED);
                                    temOrder.setStatus(ContractOptionOrderStatus.CLOSE);

                                    orderService.saveOrUpdate(temOrder);
                                }
                            }else{ 
                                for(int k = 0; k < orderList.size(); k++) {
                                    ContractOptionOrder temOrder = orderList.get(k);
                                    MemberWallet wallet = walletService.findByCoinUnitAndMemberId(temOrder.getBaseSymbol(), temOrder.getMemberId());
                                    walletService.decreaseFrozen(wallet.getId(), temOrder.getBetAmount());

                                    MemberTransaction memberTransaction = new MemberTransaction();
                                    memberTransaction.setFee(BigDecimal.ZERO);
                                    memberTransaction.setAmount(BigDecimal.ZERO.subtract(temOrder.getBetAmount()));
                                    memberTransaction.setSymbol(temOrder.getBaseSymbol());
                                    memberTransaction.setType(TransactionType.OPTION_FAIL.getCode());
                                    memberTransaction.setMemberId(temOrder.getMemberId());
                                    memberTransaction.setRealFee("0");
                                    memberTransaction.setDiscountFee("0");
                                    memberTransaction.setCreateTime(new Date());
                                    memberTransactionService.save(memberTransaction);

                                    
                                    temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getBetAmount()));

                                    if(temOrder.getFee().compareTo(BigDecimal.ZERO) > 0) {
                                        walletService.decreaseFrozen(wallet.getId(), temOrder.getFee());

                                        MemberTransaction memberTransaction1 = new MemberTransaction();
                                        memberTransaction1.setFee(BigDecimal.ZERO);
                                        memberTransaction1.setAmount(BigDecimal.ZERO.subtract(temOrder.getFee()));
                                        memberTransaction1.setSymbol(temOrder.getBaseSymbol());
                                        memberTransaction1.setType(TransactionType.OPTION_FAIL.getCode());
                                        memberTransaction1.setMemberId(temOrder.getMemberId());
                                        memberTransaction1.setRealFee("0");
                                        memberTransaction1.setDiscountFee("0");
                                        memberTransaction1.setCreateTime(new Date());
                                        memberTransactionService.save(memberTransaction1);

                                        
                                        temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getFee()));
                                    }

                                    temOrder.setResult(ContractOptionOrderResult.TIED);
                                    temOrder.setStatus(ContractOptionOrderStatus.CLOSE);

                                    orderService.saveOrUpdate(temOrder);
                                }
                            }
                        }else{
                           logger.info("{} - Round {} option contract result: Fall", temOption.getSymbol(), temOption.getOptionNo());

                            temOption.setResult(ContractOptionResult.LOSE);
                            
                            for(int k = 0; k < orderList.size(); k++) {
                                ContractOptionOrder temOrder = orderList.get(k);
                                MemberWallet wallet = walletService.findByCoinUnitAndMemberId(temOrder.getBaseSymbol(), temOrder.getMemberId());
                                if(orderList.get(k).getDirection() == ContractOptionOrderDirection.SELL) {
                                    
                                    walletService.thawBalance(temOrder.getBaseSymbol(), temOrder.getMemberId(), temOrder.getBetAmount()); 
                                    if(temOrder.getFee().compareTo(BigDecimal.ZERO) > 0) { 
                                        walletService.decreaseFrozen(wallet.getId(), temOrder.getFee());
                                        MemberTransaction memberTransaction = new MemberTransaction();
                                        memberTransaction.setFee(BigDecimal.ZERO);
                                        memberTransaction.setAmount(BigDecimal.ZERO.subtract(temOrder.getFee()));
                                        memberTransaction.setSymbol(temOrder.getBaseSymbol());
                                        memberTransaction.setType(TransactionType.OPTION_FEE.getCode());
                                        memberTransaction.setMemberId(temOrder.getMemberId());
                                        memberTransaction.setRealFee("0");
                                        memberTransaction.setDiscountFee("0");
                                        memberTransaction.setCreateTime(new Date());
                                        memberTransactionService.save(memberTransaction);

                                        
                                        temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getFee()));
                                    }
                                    
                                    BigDecimal reward = BigDecimal.ZERO;



                                    reward = temOrder.getBetAmount().multiply(coinList.get(i).getOods()).setScale(4, RoundingMode.DOWN);
                                    
                                    BigDecimal winFee = reward.multiply(coinList.get(i).getWinFeePercent());
                                    temOrder.setWinFee(winFee);
                                    if(reward.compareTo(BigDecimal.ZERO) > 0) {
                                        
                                        walletService.increaseBalance(wallet.getId(), reward.subtract(winFee));
                                        
                                        MemberTransaction memberTransaction = new MemberTransaction();
                                        memberTransaction.setFee(BigDecimal.ZERO);
                                        memberTransaction.setAmount(reward.subtract(winFee));
                                        memberTransaction.setSymbol(temOrder.getBaseSymbol());
                                        memberTransaction.setType(TransactionType.OPTION_REWARD.getCode());
                                        memberTransaction.setMemberId(temOrder.getMemberId());
                                        memberTransaction.setRealFee("0");
                                        memberTransaction.setDiscountFee("0");
                                        memberTransaction.setCreateTime(new Date());
                                        memberTransactionService.save(memberTransaction);

                                        
                                        temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getWinFee()));

                                        temOrder.setRewardAmount(reward.subtract(winFee));
                                    }

                                    temOrder.setResult(ContractOptionOrderResult.WIN);
                                    temOrder.setStatus(ContractOptionOrderStatus.CLOSE);

                                    orderService.saveOrUpdate(temOrder);
                                }else{
                                    
                                    walletService.decreaseFrozen(wallet.getId(), temOrder.getBetAmount());
                                    MemberTransaction memberTransaction = new MemberTransaction();
                                    memberTransaction.setFee(BigDecimal.ZERO);
                                    memberTransaction.setAmount(BigDecimal.ZERO.subtract(temOrder.getBetAmount()));
                                    memberTransaction.setSymbol(temOrder.getBaseSymbol());
                                    memberTransaction.setType(TransactionType.OPTION_FAIL.getCode());
                                    memberTransaction.setMemberId(temOrder.getMemberId());
                                    memberTransaction.setRealFee("0");
                                    memberTransaction.setDiscountFee("0");
                                    memberTransaction.setCreateTime(new Date());
                                    memberTransactionService.save(memberTransaction);

                                    
                                    temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getBetAmount()));

                                    if(temOrder.getFee().compareTo(BigDecimal.ZERO) > 0) { 
                                        walletService.decreaseFrozen(wallet.getId(), temOrder.getFee());
                                        MemberTransaction memberTransaction1 = new MemberTransaction();
                                        memberTransaction1.setFee(BigDecimal.ZERO);
                                        memberTransaction1.setAmount(BigDecimal.ZERO.subtract(temOrder.getFee()));
                                        memberTransaction1.setSymbol(temOrder.getBaseSymbol());
                                        memberTransaction1.setType(TransactionType.OPTION_FEE.getCode());
                                        memberTransaction1.setMemberId(temOrder.getMemberId());
                                        memberTransaction1.setRealFee("0");
                                        memberTransaction1.setDiscountFee("0");
                                        memberTransaction1.setCreateTime(new Date());
                                        memberTransactionService.save(memberTransaction1);

                                        
                                        temOption.setTotalPl(temOption.getTotalPl().add(temOrder.getFee()));
                                    }

                                    temOrder.setResult(ContractOptionOrderResult.LOSE); 
                                    temOrder.setStatus(ContractOptionOrderStatus.CLOSE); 

                                    orderService.saveOrUpdate(temOrder);
                                }
                            }
                        }
                        
                        ContractOptionCoin temCoin = coinList.get(i);
                        temCoin.setTotalProfit(temCoin.getTotalProfit().add(temOption.getTotalPl()));

                        
                        optionService.saveOrUpdate(temOption);
                    }
                }
            }
        }
    }
}

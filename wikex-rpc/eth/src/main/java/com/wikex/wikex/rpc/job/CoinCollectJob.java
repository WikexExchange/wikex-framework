package com.wikex.wikex.rpc.job;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.service.AccountService;
import com.wikex.wikex.rpc.service.EthService;
import com.wikex.wikex.rpc.util.AccountReplay;
import com.wikex.wikex.rpc.util.MessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CoinCollectJob {
    private Logger logger = LoggerFactory.getLogger(CoinCollectJob.class);
    @Autowired
    private AccountService accountService;
    @Autowired
    private EthService ethService;
    @Autowired
    private Coin coin;

    /**
     * Synchronize ETH address balance
     */
//    @Scheduled(cron = "0 0 */2 * * *")
//    @Scheduled(cron = "0 0 17 * * ?")
//    public void rechargeMinerFee(){
//        AccountReplay accountReplay = new AccountReplay(accountService,100);
//        accountReplay.run(account -> {
//            logger.info("process account:{}",account);
//            try {
//                // Query balance
//                BigDecimal ethBalance = ethService.getBalance(account.getAddress());
//                // Synchronize account balance
//                accountService.updateBalance(account.getAddress(),ethBalance);
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//        });
//    }

    /**
     * Recharge miner fee for user addresses (runs every 2 hours)
     */
    @Scheduled(cron = "0 0 */2 * * *")
    public void rechargeMinerFee(){
        try {
            logger.info("Recharging miner fee for user addresses (runs every 1 hour)");
            AccountReplay accountReplay = new AccountReplay(accountService, 100);
            BigDecimal minerFee = ethService.getMinerFee(coin.getGasLimit());

            accountReplay.run(account -> {
                try {

                    BigDecimal ethBalance = ethService.getBalance(account.getAddress());
                    List<Contract> contracts = ContractsConfig.getContracts();
                    boolean needTransferFee = false;
                    for (Contract coin : contracts) {
                        logger.info("Recharging miner fee for user address: " + JSON.toJSONString(coin));
                        BigDecimal tokenBalance = ethService.getTokenBalance(account.getAddress(), coin);
                        logger.info("Retrieved balance: " + tokenBalance.toPlainString());
                        if (ethBalance.compareTo(minerFee) < 0
                                && tokenBalance.compareTo(coin.getMinCollectAmount()) >= 0) {
                            needTransferFee = true;
                        }
                        accountService.updateBalanceByCoinName(account.getAddress(), tokenBalance, ethBalance, coin.getName());
                    }
                    // Send miner fee to addresses that meet the conditions:
                    // Condition 1: ETH amount is less than minerFee
                    // Condition 2: Token balance is greater than or equal to minCollectAmount
                    if (needTransferFee) {
                        logger.info("process account:{}, eth balance: {}, miner fee: {}, token balance: {}", account, ethBalance, minerFee);
                        // Calculate miner fee amount to transfer this time
                        BigDecimal feeAmt = minerFee.subtract(ethBalance);

                        MessageResult mr = ethService.transferFeeFromWithdrawWallet(account.getAddress(), feeAmt, false);
                        logger.info("transfer fee {}, result:{}", feeAmt, mr);
                        if (mr.getCode() == 0) {
                            ethBalance = minerFee;
                        }
                    }

                    // Synchronize account balance
                    accountService.updateBalance(account.getAddress(), ethBalance);
                } catch (Exception e) {
                    logger.info(e.getMessage());
                    e.printStackTrace();
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

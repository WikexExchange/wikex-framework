package com.wikex.wikex.rpc.job;

import com.wikex.wikex.rpc.config.ContractsConfig;
import com.wikex.wikex.rpc.entity.Contract;
import com.wikex.wikex.rpc.service.TRC20Service;
import com.wikex.wikex.rpc.service.AccountService;
import com.wikex.wikex.rpc.util.AccountReplay;
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
    private TRC20Service trc20Service;

    private BigDecimal minFee = BigDecimal.valueOf(30);

    /**
     * Address balance — runs once per hour
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void rechargeMinerFee(){
        AccountReplay accountReplay = new AccountReplay(accountService,100);
        accountReplay.run(account -> {
            logger.info("process account:{}",account);
            try {
                // Query balance
                BigDecimal balance = trc20Service.getTRXBalance(account.getAddress());
                // Sync account balance
                boolean needTransferFee = false;
                accountService.updateBalance(account.getAddress(),balance);
                List<Contract> contracts = ContractsConfig.getContracts();
                for (Contract contract : contracts) {
                    BigDecimal tokenBalance = trc20Service.getTokenBalance(account.getAddress(),contract);
                    logger.info("Update {} balance {}",contract.getName(),tokenBalance.toPlainString());
                    accountService.updateBalanceByCoinName(account.getAddress(),tokenBalance,balance,contract.getName());
                    if(tokenBalance.compareTo(contract.getMinCollectAmount())>=0 && balance.compareTo(minFee)<0){
                        needTransferFee = true;
                    }
                }
                BigDecimal subtract = minFee.subtract(balance);
                if(needTransferFee && subtract.compareTo(BigDecimal.ZERO)>=0){
                    trc20Service.transferTRXFromWithdrawWallet(account.getAddress(),subtract,true);
                }
            }
            catch (Exception e){
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

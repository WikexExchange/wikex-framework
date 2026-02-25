package com.wikex.wikex.admin.task;


import com.wikex.wikex.swap.feign.ContractOrderEntrustFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RakeBackJob {
    private Logger logger = LoggerFactory.getLogger(RakeBackJob.class);

    @Autowired
    private ContractOrderEntrustFeign contractOrderEntrustService;
    @Autowired
    private MemberTransactionFeign memberTransactionService;

    

    @XxlJob("autoRakeBack")
    public void autoRakeBack(){
        logger.info("start rake back...");
        
        memberTransactionService.updateRewardRobot();
        
        contractOrderEntrustService.sendReward();
        
        memberTransactionService.sendExchangeReward();
        
        memberTransactionService.sendSecondReward();
        
        memberTransactionService.sendOptionReward();

        logger.info("end rake back...");
    }


}

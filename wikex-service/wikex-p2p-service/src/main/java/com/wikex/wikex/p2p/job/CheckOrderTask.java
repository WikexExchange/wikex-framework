package com.wikex.wikex.p2p.job;

import com.wikex.wikex.p2p.entity.OtcOrder;
import com.wikex.wikex.p2p.service.OtcOrderService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class CheckOrderTask {
    @Autowired
    private OtcOrderService orderService;

//    @Scheduled(fixedRate = 60000)
    @XxlJob("checkExpireOrder")
    public void checkExpireOrder() {
        
        List<OtcOrder> list = orderService.checkExpiredOrder();
        list.stream().forEach(x -> {
                    try {
                        orderService.cancelOrderTask(x);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
        
    }
}

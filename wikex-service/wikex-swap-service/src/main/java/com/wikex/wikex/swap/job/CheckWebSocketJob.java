package com.wikex.wikex.swap.job;


import com.wikex.wikex.swap.util.BinanceWebSocketConnectionManage;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.java_websocket.enums.ReadyState;
import org.springframework.stereotype.Component;

@Component
public class CheckWebSocketJob {
    
//    @Scheduled(cron = "*/10 * * * * *")
    @XxlJob("checkWebSocket")
    public void checkWebSocket(){


        if(BinanceWebSocketConnectionManage.getWebSocket() != null && !BinanceWebSocketConnectionManage.getWebSocket().getReadyState().equals(ReadyState.OPEN)) {
            BinanceWebSocketConnectionManage.getWebSocket().reconnect();
        }
    }
}

package com.wikex.wikex.coinswap.job;


import com.wikex.wikex.coinswap.util.WikexWebSocketConnectionManage;
import com.wikex.wikex.coinswap.util.HuobiWebSocketConnectionManage;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.java_websocket.enums.ReadyState;
import org.springframework.stereotype.Component;

@Component
public class CheckWebSocketJob {
    
//    @Scheduled(cron = "*/10 * * * * *")
    @XxlJob("checkWebSocket")
    public void checkWebSocket(){
        if(HuobiWebSocketConnectionManage.getWebSocket() != null && !HuobiWebSocketConnectionManage.getWebSocket().getReadyState().equals(ReadyState.OPEN)) {
            HuobiWebSocketConnectionManage.getWebSocket().reconnect();
        }
        if(WikexWebSocketConnectionManage.getWebSocket() != null && !WikexWebSocketConnectionManage.getWebSocket().getReadyState().equals(ReadyState.OPEN)) {
            WikexWebSocketConnectionManage.getWebSocket().reconnect();
        }
    }
}

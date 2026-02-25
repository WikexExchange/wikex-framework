package com.wikex.wikex.second.job;

import com.wikex.wikex.second.util.WebSocketConnectionManage;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.java_websocket.enums.ReadyState;
import org.springframework.stereotype.Component;

@Component
public class CheckWebSocketJob {
    
//    @Scheduled(cron = "*/10 * * * * *")
    @XxlJob("checkWebSocket")
    public void checkWebSocket(){
        if(WebSocketConnectionManage.getWebSocket() != null && !WebSocketConnectionManage.getWebSocket().getReadyState().equals(ReadyState.OPEN)) {
            WebSocketConnectionManage.getWebSocket().reconnect();
        }
    }
}

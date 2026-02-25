package com.wikex.wikex.chat.controller;


import com.wikex.wikex.chat.entity.RealTimeChatMessage;
import com.wikex.wikex.chat.handler.NettyHandler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class WebSocketController {
    private  final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    @Autowired
    private NettyHandler nettyHandler;

    /**
     * ORDER-GENERATED
     * @param message
     */
    @MessageMapping("/message/chat")
    public void chat(RealTimeChatMessage message){
        logger.info("message={}",message);
        nettyHandler.handleMessage(message);
    }
}

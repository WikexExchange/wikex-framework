package com.wikex.wikex.chat.controller;


import com.wikex.wikex.chat.entity.HistoryChatMessage;
import com.wikex.wikex.chat.entity.HistoryMessagePage;
import com.wikex.wikex.chat.handler.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistoryMessageController {

    @Autowired
    private MessageHandler chatMessageHandler ;

    @RequestMapping("/getHistoryMessage")
    public HistoryMessagePage getHistoryMessage(HistoryChatMessage message){
        return chatMessageHandler.getHistoryMessage(message);
    }
}

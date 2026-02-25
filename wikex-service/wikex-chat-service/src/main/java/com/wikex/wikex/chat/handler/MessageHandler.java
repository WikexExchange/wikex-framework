package com.wikex.wikex.chat.handler;


import com.wikex.wikex.chat.entity.ChatMessageRecord;
import com.wikex.wikex.chat.entity.HistoryChatMessage;
import com.wikex.wikex.chat.entity.HistoryMessagePage;

public interface MessageHandler {

    void handleMessage(ChatMessageRecord message);

    HistoryMessagePage getHistoryMessage(HistoryChatMessage message);
}

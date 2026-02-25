package com.wikex.wikex.user.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikex.wikex.user.deposit.dto.DepositEvent;
import com.wikex.wikex.user.deposit.DepositEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDepositRabbitConsumer {

    private final ObjectMapper objectMapper;
    private final DepositEventHandler depositEventHandler;

    @RabbitListener(queues = "internal.deposit.events")
    public void onDepositEvent(String message) {
        try {
            log.info("[Rabbit] Received: {}", message);
            DepositEvent event = objectMapper.readValue(message, DepositEvent.class);
            depositEventHandler.handle(event);

        } catch (Exception e) {
            log.error("[Rabbit] Failed to handle message: {}", message, e);
        }
    }
}

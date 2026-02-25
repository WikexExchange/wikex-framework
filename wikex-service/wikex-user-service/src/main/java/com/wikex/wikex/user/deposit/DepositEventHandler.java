package com.wikex.wikex.user.deposit;

import com.wikex.wikex.user.deposit.dto.DepositEvent;
import com.wikex.wikex.user.deposit.dto.DepositPayload;
import com.wikex.wikex.user.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositEventHandler {

    private final DepositService depositService;
    private final SimpMessagingTemplate simp;

    public void handle(DepositEvent event) {
        if (event == null || event.getPayload() == null || event.getType() == null) {
            log.warn("[DepositEventHandler] Skip null event/payload/type");
            return;
        }

        DepositPayload payload = event.getPayload();
        String type = event.getType().toLowerCase(Locale.ROOT);

        switch (type) {
            case "deposit.detected":
                depositService.handleDepositDetected(payload);
                break;
            case "deposit.confirmed":
                depositService.handleDepositConfirmed(payload);
                break;
            case "deposit.credited":
                depositService.handleDepositCredited(payload);
                break;
            default:
                log.warn("[DepositEventHandler] Unknown type={}", event.getType());
                return;
        }

        String userId = payload.getExternalUserId();
        if (userId != null) {
            String dest = "/topic/deposit/" + userId;
            simp.convertAndSend(dest, event);
            log.info("[WS] Sent dest={} type={} txHash={}", dest, type, payload.getTxHash());
        } else {
            log.warn("[DepositEventHandler] Missing externalUserId, cannot push WS");
        }
    }
}

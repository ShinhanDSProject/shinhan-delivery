package com.example.shinhangaecheokja.tracking.service;

import com.example.shinhangaecheokja.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhangaecheokja.tracking.dto.response.DeliveryStatusBroadcastResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 배송 상태 전이(픽업 완료·배송 완료) 이벤트를 받아 구독자에게 실시간으로 브로드캐스트한다. 트랜잭션이 커밋된 이후에만 반응해, 전이가 롤백되면 브로드캐스트도 나가지 않도록
 * 한다.
 */
@Component
@RequiredArgsConstructor
public class DeliveryStatusBroadcastListener {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
    DeliveryStatusBroadcastResponse broadcast =
        new DeliveryStatusBroadcastResponse(
            event.deliveryRequestId(), event.status(), event.timestamp());
    messagingTemplate.convertAndSend(
        "/topic/delivery/" + event.deliveryRequestId() + "/status", broadcast);
  }
}

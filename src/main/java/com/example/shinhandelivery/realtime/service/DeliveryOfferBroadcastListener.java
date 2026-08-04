package com.example.shinhandelivery.realtime.service;

import com.example.shinhandelivery.delivery.event.DeliveryOfferBroadcastEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 신규 배송요청 오퍼 이벤트를 받아, 후보 차량들 각각의 개인 채널로 실시간 브로드캐스트한다. 트랜잭션이 커밋된 이후에만 반응해, 배송요청 생성이 롤백되면 오퍼도 나가지 않도록
 * 한다.
 */
@Component
@RequiredArgsConstructor
public class DeliveryOfferBroadcastListener {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onDeliveryOfferBroadcast(DeliveryOfferBroadcastEvent event) {
    event
        .candidateVehicleIds()
        .forEach(
            vehicleId ->
                messagingTemplate.convertAndSend(
                    "/topic/vehicles/" + vehicleId + "/offers", event.offer()));
  }
}

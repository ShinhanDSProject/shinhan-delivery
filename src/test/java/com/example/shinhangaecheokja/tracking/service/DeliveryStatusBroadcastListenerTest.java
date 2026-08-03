package com.example.shinhangaecheokja.tracking.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhangaecheokja.tracking.dto.response.DeliveryStatusBroadcastResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class DeliveryStatusBroadcastListenerTest {

  @Mock private SimpMessagingTemplate messagingTemplate;
  @InjectMocks private DeliveryStatusBroadcastListener listener;

  @Test
  @DisplayName("상태변경 이벤트를 받으면 해당 배송의 status 토픽으로 브로드캐스트한다")
  void onDeliveryStatusChangedShouldBroadcastToTopic() {
    LocalDateTime timestamp = LocalDateTime.of(2026, 7, 31, 12, 0);
    DeliveryStatusChangedEvent event =
        new DeliveryStatusChangedEvent(1L, DeliveryStatus.PICKED_UP, timestamp);

    listener.onDeliveryStatusChanged(event);

    verify(messagingTemplate)
        .convertAndSend(
            eq("/topic/delivery/1/status"),
            eq(new DeliveryStatusBroadcastResponse(1L, DeliveryStatus.PICKED_UP, timestamp)));
  }
}

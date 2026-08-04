package com.example.shinhangaecheokja.realtime.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.ItemSize;
import com.example.shinhangaecheokja.delivery.event.DeliveryOfferBroadcastEvent;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class DeliveryOfferBroadcastListenerTest {

  @Mock private SimpMessagingTemplate messagingTemplate;
  @InjectMocks private DeliveryOfferBroadcastListener listener;

  @Test
  @DisplayName("오퍼 이벤트를 받으면 후보 차량마다 각자의 오퍼 토픽으로 브로드캐스트한다")
  void onDeliveryOfferBroadcastShouldBroadcastToEachCandidateTopic() {
    DeliveryResponse offer =
        new DeliveryResponse(
            1L,
            10L,
            "서울시 강남구",
            "서울시 서초구",
            10,
            5,
            DeliveryStatus.REQUESTED,
            5000L,
            37.5,
            127.0,
            37.6,
            127.1,
            ItemSize.MEDIUM);
    DeliveryOfferBroadcastEvent event = new DeliveryOfferBroadcastEvent(List.of(2L, 3L), offer);

    listener.onDeliveryOfferBroadcast(event);

    verify(messagingTemplate).convertAndSend(eq("/topic/vehicles/2/offers"), eq(offer));
    verify(messagingTemplate).convertAndSend(eq("/topic/vehicles/3/offers"), eq(offer));
    verifyNoMoreInteractions(messagingTemplate);
  }
}

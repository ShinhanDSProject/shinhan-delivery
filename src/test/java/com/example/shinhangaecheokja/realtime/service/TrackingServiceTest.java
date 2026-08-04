package com.example.shinhangaecheokja.realtime.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.delivery.service.DeliveryService;
import com.example.shinhangaecheokja.delivery.service.MatchingService;
import com.example.shinhangaecheokja.realtime.exception.UnauthorizedOfferAccessException;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

  @Mock private MatchingService matchingService;
  @Mock private VehicleService vehicleService;
  @Mock private DeliveryService deliveryService;
  @Mock private SimpMessagingTemplate messagingTemplate;
  @InjectMocks private TrackingService trackingService;

  @Test
  @DisplayName("차량 소유주는 그 차량의 오퍼 채널을 구독할 수 있다")
  void assertCanSubscribeToOffersOwnerShouldPass() {
    Vehicle vehicle = Vehicle.builder().id(2L).ownerId(1L).build();
    when(vehicleService.getById(2L)).thenReturn(vehicle);

    trackingService.assertCanSubscribeToOffers(1L, 2L);
  }

  @Test
  @DisplayName("차량 소유주가 아니면 오퍼 채널 구독 시 UnauthorizedOfferAccessException을 던진다")
  void assertCanSubscribeToOffersNonOwnerShouldThrowException() {
    Vehicle vehicle = Vehicle.builder().id(2L).ownerId(1L).build();
    when(vehicleService.getById(2L)).thenReturn(vehicle);

    assertThatThrownBy(() -> trackingService.assertCanSubscribeToOffers(999L, 2L))
        .isInstanceOf(UnauthorizedOfferAccessException.class);
  }
}

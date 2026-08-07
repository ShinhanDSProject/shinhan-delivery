package com.example.shinhandelivery.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.delivery.service.MatchingService;
import com.example.shinhandelivery.notification.dto.response.NotificationResponse;
import com.example.shinhandelivery.notification.entity.Notification;
import com.example.shinhandelivery.notification.repository.NotificationRepository;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class NotificationCreateListenerTest {

  private static final Long DELIVERY_REQUEST_ID = 10L;
  private static final Long CUSTOMER_ID = 1L;
  private static final Long COURIER_ID = 2L;
  private static final Long VEHICLE_ID = 3L;

  @Mock private DeliveryService deliveryService;
  @Mock private MatchingService matchingService;
  @Mock private VehicleService vehicleService;
  @Mock private NotificationRepository notificationRepository;
  @Mock private SimpMessagingTemplate messagingTemplate;
  @InjectMocks private NotificationCreateListener listener;

  @Test
  @DisplayName("MATCHED 전이 시 고객과 배송원 모두에게 알림을 생성하고 각자 채널로 push한다")
  void onMatchedShouldNotifyBothCustomerAndCourier() {
    givenDeliveryRequestOwnedByCustomer();
    givenMatchedToCourier();
    when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    listener.onDeliveryStatusChanged(
        new DeliveryStatusChangedEvent(
            DELIVERY_REQUEST_ID, DeliveryStatus.MATCHED, LocalDateTime.now()));

    ArgumentCaptor<Notification> savedCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository, times(2)).save(savedCaptor.capture());
    assertThatMemberIdsAre(savedCaptor.getAllValues(), CUSTOMER_ID, COURIER_ID);

    verify(messagingTemplate)
        .convertAndSend(
            eq("/topic/members/" + CUSTOMER_ID + "/notifications"),
            any(NotificationResponse.class));
    verify(messagingTemplate)
        .convertAndSend(
            eq("/topic/members/" + COURIER_ID + "/notifications"), any(NotificationResponse.class));
    verifyNoMoreInteractions(messagingTemplate);
  }

  @Test
  @DisplayName("PICKED_UP 전이 시 고객에게만 알림을 보내고 배송원에게는 보내지 않는다")
  void onPickedUpShouldNotifyCustomerOnly() {
    givenDeliveryRequestOwnedByCustomer();
    when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    listener.onDeliveryStatusChanged(
        new DeliveryStatusChangedEvent(
            DELIVERY_REQUEST_ID, DeliveryStatus.PICKED_UP, LocalDateTime.now()));

    verify(notificationRepository, times(1)).save(any());
    verify(messagingTemplate)
        .convertAndSend(
            eq("/topic/members/" + CUSTOMER_ID + "/notifications"),
            any(NotificationResponse.class));
    verifyNoMoreInteractions(messagingTemplate);
    verify(matchingService, never()).getMatchingByDeliveryRequestId(anyLong());
  }

  @Test
  @DisplayName("아직 매칭되지 않은 배송 요청이면 배송원 알림 없이 고객에게만 보낸다")
  void whenNoMatchingExistsShouldNotifyCustomerOnly() {
    givenDeliveryRequestOwnedByCustomer();
    when(matchingService.getMatchingByDeliveryRequestId(DELIVERY_REQUEST_ID))
        .thenThrow(new EntityNotFoundException(ErrorCode.MATCHING_NOT_FOUND));
    when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    listener.onDeliveryStatusChanged(
        new DeliveryStatusChangedEvent(
            DELIVERY_REQUEST_ID, DeliveryStatus.CANCELLED, LocalDateTime.now()));

    verify(notificationRepository, times(1)).save(any());
    verify(messagingTemplate)
        .convertAndSend(
            eq("/topic/members/" + CUSTOMER_ID + "/notifications"),
            any(NotificationResponse.class));
    verifyNoMoreInteractions(messagingTemplate);
  }

  @Test
  @DisplayName("이 리스너가 다루지 않는 상태(REQUESTED)면 아무 알림도 만들지 않는다")
  void onRequestedShouldDoNothing() {
    listener.onDeliveryStatusChanged(
        new DeliveryStatusChangedEvent(
            DELIVERY_REQUEST_ID, DeliveryStatus.REQUESTED, LocalDateTime.now()));

    verify(notificationRepository, never()).save(any());
    verifyNoMoreInteractions(messagingTemplate);
  }

  private void givenDeliveryRequestOwnedByCustomer() {
    DeliveryRequest deliveryRequest =
        DeliveryRequest.builder().id(DELIVERY_REQUEST_ID).memberId(CUSTOMER_ID).build();
    when(deliveryService.getDeliveryRequest(DELIVERY_REQUEST_ID)).thenReturn(deliveryRequest);
  }

  private void givenMatchedToCourier() {
    Matching matching =
        Matching.builder().deliveryRequestId(DELIVERY_REQUEST_ID).vehicleId(VEHICLE_ID).build();
    when(matchingService.getMatchingByDeliveryRequestId(DELIVERY_REQUEST_ID)).thenReturn(matching);
    Vehicle vehicle = Vehicle.builder().id(VEHICLE_ID).memberId(COURIER_ID).build();
    when(vehicleService.getById(VEHICLE_ID)).thenReturn(vehicle);
  }

  private void assertThatMemberIdsAre(List<Notification> notifications, Long... expectedMemberIds) {
    List<Long> actual = new ArrayList<>();
    notifications.forEach(n -> actual.add(n.getMemberId()));
    Assertions.assertThat(actual).containsExactlyInAnyOrder(expectedMemberIds);
  }
}

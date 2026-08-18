package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.delivery.dto.response.AvailableDeliveryResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.entity.MatchingStatus;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.helper.DeliveryFeeCalculator;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DeliveryMatchingServiceTest {

  @Mock private MemberService memberService;
  @Mock private VehicleService vehicleService;
  @Mock private DeliveryRequestRepository deliveryRequestRepository;
  @Mock private MatchingRepository matchingRepository;
  @Mock private DeliveryFeeCalculator feeCalculator;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private DeliveryMatchingService deliveryMatchingService;

  @Test
  @DisplayName("반경 3km 이내의 대기 중인(REQUESTED) 주문만 거리순으로 조회된다")
  void getAvailableDeliveriesFilterAndSortByDistance() {
    // given
    Long courierId = 1L;
    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();

    DeliveryRequest nearRequest =
        DeliveryRequest.builder()
            .id(101L)
            .pickupAddress("가까운 픽업지")
            .pickupLocation(Location.of(37.5670, 126.9785))
            .dropoffLocation(Location.of(37.5600, 126.9800))
            .dropoffAddress("도착지1")
            .weight(2.0)
            .distance(1.5)
            .feePoint(4000L)
            .status(DeliveryStatus.REQUESTED)
            .itemSize(ItemSize.MEDIUM)
            .createdAt(LocalDateTime.now())
            .build();

    DeliveryRequest farRequest =
        DeliveryRequest.builder()
            .id(102L)
            .pickupAddress("먼 픽업지")
            .pickupLocation(Location.of(37.6000, 127.1000))
            .dropoffLocation(Location.of(37.5600, 126.9800))
            .dropoffAddress("도착지2")
            .weight(3.0)
            .distance(5.0)
            .feePoint(7000L)
            .status(DeliveryStatus.REQUESTED)
            .itemSize(ItemSize.LARGE)
            .createdAt(LocalDateTime.now())
            .build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(deliveryRequestRepository.findAllByStatus(DeliveryStatus.REQUESTED))
        .willReturn(List.of(nearRequest, farRequest));

    // 가까운 주문은 1.0km, 먼 주문은 10.0km로 모킹
    given(
            feeCalculator.calculateDistanceKm(
                Location.of(37.5665, 126.9780), nearRequest.getPickupLocation()))
        .willReturn(1.0);
    given(
            feeCalculator.calculateDistanceKm(
                Location.of(37.5665, 126.9780), farRequest.getPickupLocation()))
        .willReturn(10.0);

    // when
    List<AvailableDeliveryResponse> result =
        deliveryMatchingService.getAvailableDeliveries(courierId, 37.5665, 126.9780, 3.0);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getDeliveryRequestId()).isEqualTo(101L);
    assertThat(result.get(0).getDistanceToPickupKm()).isEqualTo(1.0);
  }

  @Test
  @DisplayName("정상적인 상태의 대기 주문을 수락(Catch)하면 Matching 엔티티가 생성된다")
  void catchDeliverySuccess() {
    // given
    Long courierId = 1L;
    Long deliveryRequestId = 100L;

    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();
    Vehicle vehicle =
        Vehicle.builder()
            .id(10L)
            .memberId(courierId)
            .type(VehicleType.MOTORCYCLE)
            .status(VehicleStatus.AVAILABLE)
            .build();

    DeliveryRequest request =
        DeliveryRequest.builder().id(deliveryRequestId).status(DeliveryStatus.REQUESTED).build();

    Matching expectedMatching =
        Matching.builder()
            .id(1L)
            .deliveryRequestId(deliveryRequestId)
            .vehicleId(vehicle.getId())
            .status(MatchingStatus.MATCHED)
            .matchedAt(LocalDateTime.now())
            .build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getVehiclesByMemberId(courierId)).willReturn(List.of(vehicle));
    given(deliveryRequestRepository.findByIdForUpdate(deliveryRequestId))
        .willReturn(Optional.of(request));
    given(matchingRepository.saveAndFlush(any(Matching.class))).willReturn(expectedMatching);

    // when
    Matching result = deliveryMatchingService.catchDelivery(courierId, deliveryRequestId);

    // then
    assertThat(result.getDeliveryRequestId()).isEqualTo(deliveryRequestId);
    assertThat(result.getVehicleId()).isEqualTo(vehicle.getId());
    assertThat(request.getStatus()).isEqualTo(DeliveryStatus.MATCHED);
  }

  @Test
  @DisplayName("주문을 수락(Catch)하면 MATCHED 상태 변경 이벤트가 발행되어 매칭 대기 화면의 WebSocket 구독으로 전달된다")
  void catchDeliveryPublishesStatusChangedEvent() {
    // given
    Long courierId = 1L;
    Long deliveryRequestId = 100L;

    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();
    Vehicle vehicle =
        Vehicle.builder()
            .id(10L)
            .memberId(courierId)
            .type(VehicleType.MOTORCYCLE)
            .status(VehicleStatus.AVAILABLE)
            .build();
    DeliveryRequest request =
        DeliveryRequest.builder().id(deliveryRequestId).status(DeliveryStatus.REQUESTED).build();
    Matching expectedMatching =
        Matching.builder()
            .id(1L)
            .deliveryRequestId(deliveryRequestId)
            .vehicleId(vehicle.getId())
            .status(MatchingStatus.MATCHED)
            .matchedAt(LocalDateTime.now())
            .build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getVehiclesByMemberId(courierId)).willReturn(List.of(vehicle));
    given(deliveryRequestRepository.findByIdForUpdate(deliveryRequestId))
        .willReturn(Optional.of(request));
    given(matchingRepository.saveAndFlush(any(Matching.class))).willReturn(expectedMatching);

    // when
    deliveryMatchingService.catchDelivery(courierId, deliveryRequestId);

    // then
    ArgumentCaptor<DeliveryStatusChangedEvent> eventCaptor =
        ArgumentCaptor.forClass(DeliveryStatusChangedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().deliveryRequestId()).isEqualTo(deliveryRequestId);
    assertThat(eventCaptor.getValue().status()).isEqualTo(DeliveryStatus.MATCHED);
    assertThat(eventCaptor.getValue().timestamp()).isNotNull();
  }

  @Test
  @DisplayName("이미 배정 완료된(MATCHED) 주문을 수락 시도 시 AlreadyMatchedException이 발생한다")
  void catchDeliveryAlreadyMatchedThrowsException() {
    // given
    Long courierId = 1L;
    Long deliveryRequestId = 100L;

    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();
    Vehicle vehicle =
        Vehicle.builder().id(10L).memberId(courierId).status(VehicleStatus.AVAILABLE).build();

    DeliveryRequest alreadyMatched =
        DeliveryRequest.builder().id(deliveryRequestId).status(DeliveryStatus.MATCHED).build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getVehiclesByMemberId(courierId)).willReturn(List.of(vehicle));
    given(deliveryRequestRepository.findByIdForUpdate(deliveryRequestId))
        .willReturn(Optional.of(alreadyMatched));

    // when & then
    assertThatThrownBy(() -> deliveryMatchingService.catchDelivery(courierId, deliveryRequestId))
        .isInstanceOf(AlreadyMatchedException.class);
  }
}

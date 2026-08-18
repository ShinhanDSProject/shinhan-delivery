package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.response.AvailableDeliveryResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.entity.MatchingStatus;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.exception.VehicleCapacityMismatchException;
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
  @DisplayName("차량이 감당 가능한(용량 이내) 반경 3km 이내의 대기 중인(REQUESTED) 주문만 거리순으로 조회된다")
  void getAvailableDeliveriesFilterAndSortByDistance() {
    // given
    Long courierId = 1L;
    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();
    Vehicle vehicle =
        Vehicle.builder()
            .id(10L)
            .memberId(courierId)
            .maxWeight(10.0)
            .maxDistance(20.0)
            .location(Location.of(37.5665, 126.9780))
            .status(VehicleStatus.AVAILABLE)
            .build();

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
    given(vehicleService.getActiveVehicle(courierId)).willReturn(vehicle);
    given(
            deliveryRequestRepository.findByStatusAndWeightLessThanEqualAndDistanceLessThanEqual(
                DeliveryStatus.REQUESTED, vehicle.getMaxWeight(), vehicle.getMaxDistance()))
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
  @DisplayName("차량 용량(무게·거리)을 초과하는 주문은 DB 조회 조건 자체에서 걸러져 목록에 나오지 않는다")
  void getAvailableDeliveriesFiltersByVehicleCapacityAtQueryLevel() {
    // given
    Long courierId = 1L;
    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();
    Vehicle bicycle =
        Vehicle.builder()
            .id(11L)
            .memberId(courierId)
            .maxWeight(10.0)
            .maxDistance(5.0)
            .location(Location.of(37.5665, 126.9780))
            .status(VehicleStatus.AVAILABLE)
            .build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getActiveVehicle(courierId)).willReturn(bicycle);
    given(
            deliveryRequestRepository.findByStatusAndWeightLessThanEqualAndDistanceLessThanEqual(
                DeliveryStatus.REQUESTED, bicycle.getMaxWeight(), bicycle.getMaxDistance()))
        .willReturn(List.of());

    // when
    List<AvailableDeliveryResponse> result =
        deliveryMatchingService.getAvailableDeliveries(courierId, 37.5665, 126.9780, 3.0);

    // then: 대형(500kg) 주문은 애초에 리포지토리 조회 조건에 안 맞아 결과에 없음 — 자바 스트림에서 걸러낸 게 아니라 쿼리 자체가 걸러줬는지 확인
    assertThat(result).isEmpty();
    verify(deliveryRequestRepository)
        .findByStatusAndWeightLessThanEqualAndDistanceLessThanEqual(
            eq(DeliveryStatus.REQUESTED), eq(bicycle.getMaxWeight()), eq(bicycle.getMaxDistance()));
  }

  @Test
  @DisplayName("등록된 운송수단이 없는 배송원이 대기열을 조회하면 예외가 발생한다")
  void getAvailableDeliveriesThrowsWhenNoVehicle() {
    // given
    Long courierId = 1L;
    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getActiveVehicle(courierId))
        .willThrow(new BusinessException(ErrorCode.VEHICLE_NOT_FOUND, "활성화된 운송수단이 없습니다."));

    // when & then
    assertThatThrownBy(
            () -> deliveryMatchingService.getAvailableDeliveries(courierId, 37.5665, 126.9780, 3.0))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("정상적인 상태의 대기 주문을 수락(Catch)하면 Matching 엔티티가 생성되고 차량이 BUSY로 전환된다")
  void catchDeliverySuccess() {
    // given
    Long courierId = 1L;
    Long deliveryRequestId = 100L;
    Long vehicleId = 10L;

    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();
    Vehicle lockedVehicle =
        Vehicle.builder()
            .id(vehicleId)
            .memberId(courierId)
            .type(VehicleType.MOTORCYCLE)
            .maxWeight(10.0)
            .maxDistance(20.0)
            .status(VehicleStatus.AVAILABLE)
            .build();

    DeliveryRequest request =
        DeliveryRequest.builder()
            .id(deliveryRequestId)
            .status(DeliveryStatus.REQUESTED)
            .weight(2.0)
            .distance(1.5)
            .build();

    Matching expectedMatching =
        Matching.builder()
            .id(1L)
            .deliveryRequestId(deliveryRequestId)
            .vehicleId(vehicleId)
            .status(MatchingStatus.MATCHED)
            .matchedAt(LocalDateTime.now())
            .build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getActiveVehicleId(courierId)).willReturn(vehicleId);
    given(deliveryRequestRepository.findByIdForUpdate(deliveryRequestId))
        .willReturn(Optional.of(request));
    given(vehicleService.getVehicleForUpdate(vehicleId)).willReturn(lockedVehicle);
    given(matchingRepository.saveAndFlush(any(Matching.class))).willReturn(expectedMatching);

    // when
    Matching result = deliveryMatchingService.catchDelivery(courierId, deliveryRequestId);

    // then
    assertThat(result.getDeliveryRequestId()).isEqualTo(deliveryRequestId);
    assertThat(result.getVehicleId()).isEqualTo(vehicleId);
    assertThat(request.getStatus()).isEqualTo(DeliveryStatus.MATCHED);
    verify(vehicleService).markBusy(vehicleId);
  }

  @Test
  @DisplayName("주문을 수락(Catch)하면 MATCHED 상태 변경 이벤트가 발행되어 매칭 대기 화면의 WebSocket 구독으로 전달된다")
  void catchDeliveryPublishesStatusChangedEvent() {
    // given
    Long courierId = 1L;
    Long deliveryRequestId = 100L;
    Long vehicleId = 10L;

    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();
    Vehicle lockedVehicle =
        Vehicle.builder()
            .id(vehicleId)
            .memberId(courierId)
            .type(VehicleType.MOTORCYCLE)
            .maxWeight(10.0)
            .maxDistance(20.0)
            .status(VehicleStatus.AVAILABLE)
            .build();
    DeliveryRequest request =
        DeliveryRequest.builder()
            .id(deliveryRequestId)
            .status(DeliveryStatus.REQUESTED)
            .weight(2.0)
            .distance(1.5)
            .build();
    Matching expectedMatching =
        Matching.builder()
            .id(1L)
            .deliveryRequestId(deliveryRequestId)
            .vehicleId(vehicleId)
            .status(MatchingStatus.MATCHED)
            .matchedAt(LocalDateTime.now())
            .build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getActiveVehicleId(courierId)).willReturn(vehicleId);
    given(deliveryRequestRepository.findByIdForUpdate(deliveryRequestId))
        .willReturn(Optional.of(request));
    given(vehicleService.getVehicleForUpdate(vehicleId)).willReturn(lockedVehicle);
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
  @DisplayName("이미 배정 완료된(MATCHED) 주문을 수락 시도 시 AlreadyMatchedException이 발생하고 차량 락은 시도조차 하지 않는다")
  void catchDeliveryAlreadyMatchedThrowsException() {
    // given
    Long courierId = 1L;
    Long deliveryRequestId = 100L;

    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();

    DeliveryRequest alreadyMatched =
        DeliveryRequest.builder().id(deliveryRequestId).status(DeliveryStatus.MATCHED).build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getActiveVehicleId(courierId)).willReturn(10L);
    given(deliveryRequestRepository.findByIdForUpdate(deliveryRequestId))
        .willReturn(Optional.of(alreadyMatched));

    // when & then
    assertThatThrownBy(() -> deliveryMatchingService.catchDelivery(courierId, deliveryRequestId))
        .isInstanceOf(AlreadyMatchedException.class);
    verify(vehicleService, never()).getVehicleForUpdate(any());
  }

  @Test
  @DisplayName("차량이 감당 못 하는 무게·거리의 주문을 수락 시도하면 VehicleCapacityMismatchException이 발생하고 매칭이 생성되지 않는다")
  void catchDeliveryExceedsVehicleCapacityThrowsException() {
    // given
    Long courierId = 1L;
    Long deliveryRequestId = 100L;
    Long vehicleId = 10L;

    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();
    Vehicle smallBicycle =
        Vehicle.builder()
            .id(vehicleId)
            .memberId(courierId)
            .type(VehicleType.BICYCLE)
            .maxWeight(10.0)
            .maxDistance(5.0)
            .status(VehicleStatus.AVAILABLE)
            .build();
    DeliveryRequest heavyRequest =
        DeliveryRequest.builder()
            .id(deliveryRequestId)
            .status(DeliveryStatus.REQUESTED)
            .weight(500.0)
            .distance(1.0)
            .build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getActiveVehicleId(courierId)).willReturn(vehicleId);
    given(deliveryRequestRepository.findByIdForUpdate(deliveryRequestId))
        .willReturn(Optional.of(heavyRequest));
    given(vehicleService.getVehicleForUpdate(vehicleId)).willReturn(smallBicycle);

    // when & then
    assertThatThrownBy(() -> deliveryMatchingService.catchDelivery(courierId, deliveryRequestId))
        .isInstanceOf(VehicleCapacityMismatchException.class);
    verify(matchingRepository, never()).saveAndFlush(any());
    verify(vehicleService, never()).markBusy(any());
  }

  @Test
  @DisplayName("영업 중(ONLINE)이 아닌 차량으로 수락 시도하면 예외가 발생한다")
  void catchDeliveryVehicleNotAvailableThrowsException() {
    // given
    Long courierId = 1L;
    Long deliveryRequestId = 100L;
    Long vehicleId = 10L;

    Member courier = Member.builder().id(courierId).role(MemberRole.COURIER).build();
    Vehicle busyVehicle =
        Vehicle.builder().id(vehicleId).memberId(courierId).status(VehicleStatus.BUSY).build();
    DeliveryRequest request =
        DeliveryRequest.builder().id(deliveryRequestId).status(DeliveryStatus.REQUESTED).build();

    given(memberService.getById(courierId)).willReturn(courier);
    given(vehicleService.getActiveVehicleId(courierId)).willReturn(vehicleId);
    given(deliveryRequestRepository.findByIdForUpdate(deliveryRequestId))
        .willReturn(Optional.of(request));
    given(vehicleService.getVehicleForUpdate(vehicleId)).willReturn(busyVehicle);

    // when & then
    assertThatThrownBy(() -> deliveryMatchingService.catchDelivery(courierId, deliveryRequestId))
        .isInstanceOf(BusinessException.class);
    verify(matchingRepository, never()).saveAndFlush(any());
  }
}

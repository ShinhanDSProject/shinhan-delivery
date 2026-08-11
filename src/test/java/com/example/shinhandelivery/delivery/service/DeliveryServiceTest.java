package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryPayLocationRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryPayRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhandelivery.delivery.dto.response.DeliveryDetailResponseDto;
import com.example.shinhandelivery.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryPaymentResponse;
import com.example.shinhandelivery.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryInstructionType;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.event.DeliveryOfferBroadcastEvent;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.exception.DeliveryAccessDeniedException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryDistanceException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryWeightException;
import com.example.shinhandelivery.delivery.exception.ProofPhotoNotFoundException;
import com.example.shinhandelivery.delivery.helper.DeliveryFeeCalculator;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.payment.dto.response.PointUseResultResponse;
import com.example.shinhandelivery.payment.service.PaymentService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

  @Mock private DeliveryRequestRepository deliveryRequestRepository;
  @Mock private MemberService memberService;
  @Mock private VehicleService vehicleService;
  @Mock private MatchingRepository matchingRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private PaymentService paymentService;
  @Spy private DeliveryFeeCalculator deliveryFeeCalculator = new DeliveryFeeCalculator();
  @InjectMocks private DeliveryService deliveryService;

  @Test
  @DisplayName("고객이 존재하면 REQUESTED 상태로 배송을 요청한다")
  void requestDeliverySuccess() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setPickupAddress("서울시 강남구");
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(38.0);
    request.setDropoffLongitude(127.0);
    request.setItemSize(ItemSize.MEDIUM);

    when(deliveryRequestRepository.save(any(DeliveryRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    DeliveryRequest response = deliveryService.requestDelivery(1L, request);

    assertThat(response.getMemberId()).isEqualTo(1L);
    assertThat(response.getFeePoint()).isEqualTo(78776L);
    assertThat(response.getStatus()).isEqualTo(DeliveryStatus.REQUESTED);
  }

  @Test
  @DisplayName("공동현관 출입번호 전달 지침을 배송 요청에 저장한다")
  void requestDeliveryWithEntranceCodeSuccess() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setPickupAddress("서울시 강남구");
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(38.0);
    request.setDropoffLongitude(127.0);
    request.setItemSize(ItemSize.MEDIUM);
    request.setDeliveryInstructionType(DeliveryInstructionType.ENTRANCE_CODE);
    request.setEntranceCode("#1234*");
    request.setUnitDetail("101동 1403호");

    when(deliveryRequestRepository.save(any(DeliveryRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    DeliveryRequest response = deliveryService.requestDelivery(1L, request);

    assertThat(response.getDeliveryInstructionType())
        .isEqualTo(DeliveryInstructionType.ENTRANCE_CODE);
    assertThat(response.getEntranceCode()).isEqualTo("#1234*");
    assertThat(response.getUnitDetail()).isEqualTo("101동 1403호");
  }

  @Test
  @DisplayName("공동현관 출입번호 방식을 선택하고 번호를 누락하면 요청을 거절한다")
  void requestDeliveryWithoutRequiredEntranceCodeShouldThrowException() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setPickupAddress("서울시 강남구");
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(38.0);
    request.setDropoffLongitude(127.0);
    request.setItemSize(ItemSize.MEDIUM);
    request.setDeliveryInstructionType(DeliveryInstructionType.ENTRANCE_CODE);

    assertThatThrownBy(() -> deliveryService.requestDelivery(1L, request))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("오퍼 후보 차량이 있으면 DeliveryOfferBroadcastEvent를 발행한다")
  void requestDeliveryWithCandidatesShouldPublishOfferEvent() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setPickupAddress("서울시 강남구");
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(38.0);
    request.setDropoffLongitude(127.0);
    request.setItemSize(ItemSize.MEDIUM);

    when(deliveryRequestRepository.save(any(DeliveryRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    Vehicle candidate = Vehicle.builder().id(2L).memberId(20L).build();
    when(vehicleService.findOfferCandidates(anyDouble(), anyDouble()))
        .thenReturn(List.of(candidate));

    deliveryService.requestDelivery(1L, request);

    ArgumentCaptor<DeliveryOfferBroadcastEvent> eventCaptor =
        ArgumentCaptor.forClass(DeliveryOfferBroadcastEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().candidateVehicleIds()).containsExactly(2L);
    assertThat(eventCaptor.getValue().offer().memberId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("오퍼 후보 차량이 없으면 DeliveryOfferBroadcastEvent를 발행하지 않는다")
  void requestDeliveryWithoutCandidatesShouldNotPublishOfferEvent() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setPickupAddress("서울시 강남구");
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(38.0);
    request.setDropoffLongitude(127.0);
    request.setItemSize(ItemSize.MEDIUM);

    when(deliveryRequestRepository.save(any(DeliveryRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(vehicleService.findOfferCandidates(anyDouble(), anyDouble())).thenReturn(List.of());

    deliveryService.requestDelivery(1L, request);

    verify(eventPublisher, never()).publishEvent(any(DeliveryOfferBroadcastEvent.class));
  }

  @Test
  @DisplayName("배송 결제로 생성된 신규 요청도 오퍼 후보가 있으면 DeliveryOfferBroadcastEvent를 발행한다")
  void payDeliveryWithCandidatesShouldPublishOfferEvent() {
    DeliveryPayRequest request = new DeliveryPayRequest();
    request.setPickup(createPayLocation("서울 강남구", 37.0, 127.0));
    request.setDropoff(createPayLocation("서울 서초구", 38.0, 127.0));
    request.setWeight(10.0);
    request.setItemSize(ItemSize.MEDIUM);

    when(paymentService.usePoint(eq(1L), eq("idem-pay"), any()))
        .thenReturn(new PointUseResultResponse(2000L, 3000L, LocalDateTime.now()));
    when(deliveryRequestRepository.findByMemberIdAndPaymentIdempotencyKey(1L, "idem-pay"))
        .thenReturn(Optional.empty());
    when(deliveryRequestRepository.save(any(DeliveryRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    Vehicle candidate = Vehicle.builder().id(2L).memberId(20L).build();
    when(vehicleService.findOfferCandidates(anyDouble(), anyDouble()))
        .thenReturn(List.of(candidate));

    DeliveryPaymentResponse response = deliveryService.payDelivery(1L, "idem-pay", request);

    ArgumentCaptor<DeliveryOfferBroadcastEvent> eventCaptor =
        ArgumentCaptor.forClass(DeliveryOfferBroadcastEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(response.deliveryStatus()).isEqualTo(DeliveryStatus.REQUESTED);
    assertThat(eventCaptor.getValue().candidateVehicleIds()).containsExactly(2L);
    assertThat(eventCaptor.getValue().offer().memberId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("존재하지 않는 고객이면 EntityNotFoundException을 던진다")
  void requestDeliveryCustomerNotFoundShouldThrowException() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setWeight(10);

    when(memberService.getById(999L))
        .thenThrow(new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

    assertThatThrownBy(() -> deliveryService.requestDelivery(999L, request))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("무게가 0이하면 InvalidDeliveryWeightException을 던진다")
  void requestDeliveryInvalidWeightShouldThrowException() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setWeight(-5);
    request.setItemSize(ItemSize.MEDIUM);

    when(memberService.getById(1L)).thenReturn(new Member());

    assertThatThrownBy(() -> deliveryService.requestDelivery(1L, request))
        .isInstanceOf(InvalidDeliveryWeightException.class);
  }

  @Test
  @DisplayName("출발지와 도착지 좌표가 같으면 InvalidDeliveryDistanceException을 던진다")
  void requestDeliverySameCoordinatesShouldThrowException() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setWeight(10);
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(37.0);
    request.setDropoffLongitude(127.0);
    request.setItemSize(ItemSize.MEDIUM);

    when(memberService.getById(1L)).thenReturn(new Member());

    assertThatThrownBy(() -> deliveryService.requestDelivery(1L, request))
        .isInstanceOf(InvalidDeliveryDistanceException.class);
  }

  @Test
  @DisplayName("존재하지 않는 배송 요청을 조회하면 EntityNotFoundException을 던진다")
  void getDeliveryRequestNotFoundShouldThrowException() {
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deliveryService.getDeliveryRequest(1L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("존재하는 배송 요청을 조회하면 DeliveryRequest를 반환한다")
  void getDeliveryRequestSuccess() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryRequest response = deliveryService.getDeliveryRequest(1L);

    assertThat(response.getMemberId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("REQUESTED 상태면 주소를 수정한다")
  void updateDeliveryRequestAddressSuccess() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    deliveryRequest.setPickupAddress("서울시 강남구");
    deliveryRequest.setDropoffAddress("서울시 서초구");
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryUpdateRequest request = new DeliveryUpdateRequest();
    request.setPickupAddress("서울시 송파구");
    request.setDropoffAddress("서울시 강동구");

    DeliveryRequest response = deliveryService.updateDeliveryRequest(1L, request);

    assertThat(response.getPickupAddress()).isEqualTo("서울시 송파구");
    assertThat(response.getDropoffAddress()).isEqualTo("서울시 강동구");
  }

  @Test
  @DisplayName("REQUESTED가 아니면 주소 수정 시 AlreadyMatchedException을 던진다")
  void updateDeliveryRequestNotRequestedAddressShouldThrowException() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryUpdateRequest request = new DeliveryUpdateRequest();
    request.setPickupAddress("서울시 송파구");
    request.setDropoffAddress("서울시 강동구");

    assertThatThrownBy(() -> deliveryService.updateDeliveryRequest(1L, request))
        .isInstanceOf(AlreadyMatchedException.class);
  }

  @Test
  @DisplayName("매칭이 없으면 배송 요청을 삭제한다")
  void deleteDeliveryRequestSuccess() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));
    when(matchingRepository.existsByDeliveryRequestId(1L)).thenReturn(false);

    deliveryService.deleteDeliveryRequest(1L);

    verify(deliveryRequestRepository).delete(deliveryRequest);
  }

  @Test
  @DisplayName("매칭된 배송 요청을 삭제하려하면 AlreadyMatchedException을 던진다")
  void deleteDeliveryRequestAlreadyMatchedShouldThrowException() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));
    when(matchingRepository.existsByDeliveryRequestId(1L)).thenReturn(true);

    assertThatThrownBy(() -> deliveryService.deleteDeliveryRequest(1L))
        .isInstanceOf(AlreadyMatchedException.class);
    verify(deliveryRequestRepository, never()).delete(any(DeliveryRequest.class));
  }

  @Test
  @DisplayName("출발지와 도착지 좌표가 같으면 거리할증 없이 기본료 무게할증 크기할증만 반환한다")
  void estimateFeeSameLocationShouldCalculateBaseFeeOnly() {
    DeliveryEstimateRequest request = new DeliveryEstimateRequest();
    request.setPickupLatitude(37.5665);
    request.setPickupLongitude(126.9780);
    request.setDestinationLatitude(37.5665);
    request.setDestinationLongitude(126.9780);
    request.setWeight(10.0);
    request.setItemSize(ItemSize.MEDIUM);

    DeliveryEstimateResponse response = deliveryService.estimateFee(request);

    assertThat(response.baseFee()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    assertThat(response.distanceSurcharge()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.weightSurcharge()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    assertThat(response.sizeSurcharge()).isEqualByComparingTo(BigDecimal.valueOf(1500));
    assertThat(response.totalFee()).isEqualByComparingTo(BigDecimal.valueOf(6500));
  }

  @Test
  @DisplayName("서울에서 부산까지 거리 무게 크기를 반영해 요금을 산정한다")
  void estimateFeeLongDistanceShouldReflectDistanceWeightSize() {
    DeliveryEstimateRequest request = new DeliveryEstimateRequest();
    request.setPickupLatitude(37.5665);
    request.setPickupLongitude(126.9780);
    request.setDestinationLatitude(35.1796);
    request.setDestinationLongitude(129.0756);
    request.setWeight(10.0);
    request.setItemSize(ItemSize.MEDIUM);

    DeliveryEstimateResponse response = deliveryService.estimateFee(request);

    assertThat(response.baseFee()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    assertThat(response.distanceSurcharge()).isEqualByComparingTo(BigDecimal.valueOf(162556));
    assertThat(response.weightSurcharge()).isEqualByComparingTo(BigDecimal.valueOf(2000));
    assertThat(response.sizeSurcharge()).isEqualByComparingTo(BigDecimal.valueOf(50267));
    assertThat(response.totalFee()).isEqualByComparingTo(BigDecimal.valueOf(217823));
  }

  @Test
  @DisplayName("물품 크기가 SMALL이면 크기할증이 없다")
  void estimateFeeSmallItemShouldHaveNoSizeSurcharge() {
    DeliveryEstimateRequest request = new DeliveryEstimateRequest();
    request.setPickupLatitude(37.5665);
    request.setPickupLongitude(126.9780);
    request.setDestinationLatitude(37.5665);
    request.setDestinationLongitude(126.9780);
    request.setWeight(10.0);
    request.setItemSize(ItemSize.SMALL);

    DeliveryEstimateResponse response = deliveryService.estimateFee(request);

    assertThat(response.sizeSurcharge()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(response.totalFee()).isEqualByComparingTo(BigDecimal.valueOf(5000));
  }

  @Test
  @DisplayName("물품 크기가 LARGE이면 60퍼센트 할증이 붙는다")
  void estimateFeeLargeItemShouldHave60PercentSurcharge() {
    DeliveryEstimateRequest request = new DeliveryEstimateRequest();
    request.setPickupLatitude(37.5665);
    request.setPickupLongitude(126.9780);
    request.setDestinationLatitude(37.5665);
    request.setDestinationLongitude(126.9780);
    request.setWeight(10.0);
    request.setItemSize(ItemSize.LARGE);

    DeliveryEstimateResponse response = deliveryService.estimateFee(request);

    assertThat(response.sizeSurcharge()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    assertThat(response.totalFee()).isEqualByComparingTo(BigDecimal.valueOf(8000));
  }

  @Test
  @DisplayName("견적 금액과 실제 배송 요청 금액이 동일한 입력에 대해 항상 일치한다")
  void estimateFeeMatchesActualRequestFee() {
    DeliveryEstimateRequest estimateRequest = new DeliveryEstimateRequest();
    estimateRequest.setPickupLatitude(37.0);
    estimateRequest.setPickupLongitude(127.0);
    estimateRequest.setDestinationLatitude(38.0);
    estimateRequest.setDestinationLongitude(127.0);
    estimateRequest.setWeight(10.0);
    estimateRequest.setItemSize(ItemSize.MEDIUM);

    DeliveryCreateRequest createRequest = new DeliveryCreateRequest();
    createRequest.setPickupAddress("서울시 강남구");
    createRequest.setDropoffAddress("서울시 서초구");
    createRequest.setWeight(10);
    createRequest.setPickupLatitude(37.0);
    createRequest.setPickupLongitude(127.0);
    createRequest.setDropoffLatitude(38.0);
    createRequest.setDropoffLongitude(127.0);
    createRequest.setItemSize(ItemSize.MEDIUM);
    when(deliveryRequestRepository.save(any(DeliveryRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    DeliveryEstimateResponse estimate = deliveryService.estimateFee(estimateRequest);
    DeliveryRequest created = deliveryService.requestDelivery(1L, createRequest);

    assertThat(created.getFeePoint()).isEqualTo(estimate.totalFee().longValueExact());
  }

  @Test
  @DisplayName("PICKED_UP 상태면 완료 처리하고 증거사진 URL을 저장한다")
  void completeDeliverySavePhotoUrlSuccess() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.PICKED_UP);
    when(deliveryRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    DeliveryRequest response = deliveryService.completeDelivery(1L, request);

    assertThat(response.getStatus()).isEqualTo(DeliveryStatus.COMPLETED);
    assertThat(deliveryRequest.getProofPhotoUrl()).isEqualTo("https://example.com/proof.jpg");
    assertThat(deliveryRequest.getCompletedAt()).isNotNull();
    verify(eventPublisher)
        .publishEvent(
            argThat(
                (DeliveryStatusChangedEvent event) ->
                    event.deliveryRequestId().equals(1L)
                        && event.status() == DeliveryStatus.COMPLETED));
  }

  @Test
  @DisplayName("PICKED_UP이 아니면 완료 처리 시 InvalidDeliveryTransitionException을 던진다")
  void completeDeliveryInvalidStatusShouldThrowException() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    assertThatThrownBy(() -> deliveryService.completeDelivery(1L, request))
        .isInstanceOf(InvalidDeliveryTransitionException.class);
  }

  @Test
  @DisplayName("MATCHED 상태면 픽업 완료로 전이하고 픽업시각을 저장한다")
  void confirmPickupSuccess() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryRequest response = deliveryService.confirmPickup(1L);

    assertThat(response.getStatus()).isEqualTo(DeliveryStatus.PICKED_UP);
    assertThat(deliveryRequest.getPickedUpAt()).isNotNull();
    verify(eventPublisher)
        .publishEvent(
            argThat(
                (DeliveryStatusChangedEvent event) ->
                    event.deliveryRequestId().equals(1L)
                        && event.status() == DeliveryStatus.PICKED_UP));
  }

  @Test
  @DisplayName("MATCHED가 아니면 픽업 처리 시 InvalidDeliveryTransitionException을 던진다")
  void confirmPickupInvalidStatusShouldThrowException() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    when(deliveryRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(deliveryRequest));

    assertThatThrownBy(() -> deliveryService.confirmPickup(1L))
        .isInstanceOf(InvalidDeliveryTransitionException.class);
  }

  @Test
  @DisplayName("고객 본인이 조회하면 완료된 배송의 증거사진을 조회한다")
  void getProofPhotoSuccess() {
    LocalDateTime completedAt = LocalDateTime.of(2026, 7, 31, 10, 0);
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);
    deliveryRequest.setProofPhotoUrl("https://example.com/proof.jpg");
    deliveryRequest.setCompletedAt(completedAt);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    ProofPhotoResponse response = deliveryService.getProofPhoto(1L, 1L);

    assertThat(response.proofPhotoUrl()).isEqualTo("https://example.com/proof.jpg");
    assertThat(response.completedAt()).isEqualTo(completedAt);
  }

  @Test
  @DisplayName("배정된 배송원이 조회하면 완료된 배송의 증거사진을 조회한다")
  void getProofPhotoAssignedCourierSuccess() {
    LocalDateTime completedAt = LocalDateTime.of(2026, 7, 31, 10, 0);
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);
    deliveryRequest.setProofPhotoUrl("https://example.com/proof.jpg");
    deliveryRequest.setCompletedAt(completedAt);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    Matching matching = new Matching();
    matching.setVehicleId(10L);
    when(matchingRepository.findByDeliveryRequestId(1L)).thenReturn(Optional.of(matching));

    Vehicle vehicle = Vehicle.builder().id(10L).memberId(20L).build();
    when(vehicleService.getById(10L)).thenReturn(vehicle);

    ProofPhotoResponse response = deliveryService.getProofPhoto(20L, 1L);

    assertThat(response.proofPhotoUrl()).isEqualTo("https://example.com/proof.jpg");
  }

  @Test
  @DisplayName("고객 본인도 배정된 배송원도 아니면 DeliveryAccessDeniedException을 던진다")
  void getProofPhotoNonOwnerNonCourierShouldThrowException() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);
    deliveryRequest.setProofPhotoUrl("https://example.com/proof.jpg");
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    assertThatThrownBy(() -> deliveryService.getProofPhoto(999L, 1L))
        .isInstanceOf(DeliveryAccessDeniedException.class);
  }

  @Test
  @DisplayName("완료되지 않은 배송의 증거사진 조회 시 ProofPhotoNotFoundException을 던진다")
  void getProofPhotoNotCompletedShouldThrowException() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    assertThatThrownBy(() -> deliveryService.getProofPhoto(1L, 1L))
        .isInstanceOf(ProofPhotoNotFoundException.class);
  }

  @Test
  @DisplayName("완료된 배송이어도 증거사진 URL이 없으면 ProofPhotoNotFoundException을 던진다")
  void getProofPhotoMissingUrlShouldThrowException() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    assertThatThrownBy(() -> deliveryService.getProofPhoto(1L, 1L))
        .isInstanceOf(ProofPhotoNotFoundException.class);
  }

  @Test
  @DisplayName("status가 없으면 회원의 전체 배송 내역을 최신순으로 조회한다")
  void getMyDeliveryRequestsWithoutStatusReturnsAll() {
    Pageable pageable = PageRequest.of(0, 10);
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);
    when(deliveryRequestRepository.findByMemberIdOrderByCreatedAtDescIdDesc(1L, pageable))
        .thenReturn(new PageImpl<>(List.of(deliveryRequest)));

    Page<DeliveryRequest> result = deliveryService.getMyDeliveryRequests(1L, null, pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getStatus()).isEqualTo(DeliveryStatus.COMPLETED);
    verify(deliveryRequestRepository, never())
        .findByMemberIdAndStatusOrderByCreatedAtDescIdDesc(any(), any(), any());
  }

  @Test
  @DisplayName("status가 있으면 그 상태로만 필터링해 조회한다")
  void getMyDeliveryRequestsWithStatusFiltersByStatus() {
    Pageable pageable = PageRequest.of(0, 10);
    when(deliveryRequestRepository.findByMemberIdAndStatusOrderByCreatedAtDescIdDesc(
            1L, DeliveryStatus.CANCELLED, pageable))
        .thenReturn(new PageImpl<>(List.of()));

    Page<DeliveryRequest> result =
        deliveryService.getMyDeliveryRequests(1L, DeliveryStatus.CANCELLED, pageable);

    assertThat(result.getContent()).isEmpty();
    verify(deliveryRequestRepository, never())
        .findByMemberIdOrderByCreatedAtDescIdDesc(any(), any());
  }

  @Test
  @DisplayName("고객 본인이 조회하고 매칭된 배송원이 있으면 상세조회에 배송원 이름이 담긴다")
  void getDeliveryRequestDetailWithCourierIncludesCourierName() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    Matching matching = new Matching();
    matching.setVehicleId(10L);
    when(matchingRepository.findByDeliveryRequestId(1L)).thenReturn(Optional.of(matching));

    Vehicle vehicle = Vehicle.builder().id(10L).memberId(20L).build();
    when(vehicleService.getById(10L)).thenReturn(vehicle);

    Member courier = new Member();
    courier.setName("박배송");
    when(memberService.getById(20L)).thenReturn(courier);

    DeliveryDetailResponseDto response = deliveryService.getDeliveryRequestDetail(1L, 1L);

    assertThat(response.courierName()).isEqualTo("박배송");
  }

  @Test
  @DisplayName("고객 본인이 조회하고 매칭된 배송원이 없으면 상세조회의 배송원 이름은 null이다")
  void getDeliveryRequestDetailWithoutCourierHasNullCourierName() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));
    when(matchingRepository.findByDeliveryRequestId(1L)).thenReturn(Optional.empty());

    DeliveryDetailResponseDto response = deliveryService.getDeliveryRequestDetail(1L, 1L);

    assertThat(response.courierName()).isNull();
  }

  @Test
  @DisplayName("배정된 배송원 본인이 조회하면 상세조회가 허용된다")
  void getDeliveryRequestDetailAssignedCourierCanView() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    Matching matching = new Matching();
    matching.setVehicleId(10L);
    when(matchingRepository.findByDeliveryRequestId(1L)).thenReturn(Optional.of(matching));

    Vehicle vehicle = Vehicle.builder().id(10L).memberId(20L).build();
    when(vehicleService.getById(10L)).thenReturn(vehicle);

    Member courier = new Member();
    courier.setName("박배송");
    when(memberService.getById(20L)).thenReturn(courier);

    DeliveryDetailResponseDto response = deliveryService.getDeliveryRequestDetail(20L, 1L);

    assertThat(response.courierName()).isEqualTo("박배송");
  }

  @Test
  @DisplayName("고객 본인도 배정된 배송원도 아니면 DeliveryAccessDeniedException을 던진다")
  void getDeliveryRequestDetailNonOwnerNonCourierShouldThrowException() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(1L);
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));
    when(matchingRepository.findByDeliveryRequestId(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deliveryService.getDeliveryRequestDetail(999L, 1L))
        .isInstanceOf(DeliveryAccessDeniedException.class);
  }

  private DeliveryPayLocationRequest createPayLocation(String address, double lat, double lng) {
    DeliveryPayLocationRequest location = new DeliveryPayLocationRequest();
    location.setAddress(address);
    location.setLat(lat);
    location.setLng(lng);
    return location;
  }
}

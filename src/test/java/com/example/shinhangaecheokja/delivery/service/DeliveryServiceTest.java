package com.example.shinhangaecheokja.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.ItemSize;
import com.example.shinhangaecheokja.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryDistanceException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryWeightException;
import com.example.shinhangaecheokja.delivery.exception.ProofPhotoNotFoundException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.member.service.MemberService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

  @Mock private DeliveryRequestRepository deliveryRequestRepository;
  @Mock private MemberService memberService;
  @Mock private MatchingRepository matchingRepository;
  @Mock private ApplicationEventPublisher eventPublisher;
  @InjectMocks private DeliveryService deliveryService;

  @Test
  void 고객이_존재하면_REQUESTED_상태로_배송을_요청한다() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(1L);
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

    DeliveryResponse response = deliveryService.requestDelivery(request);

    assertThat(response.customerId()).isEqualTo(1L);
    assertThat(response.feePoint()).isEqualTo(78776L);
    assertThat(response.status()).isEqualTo(DeliveryStatus.REQUESTED);
  }

  @Test
  void 존재하지_않는_고객이면_EntityNotFoundException을_던진다() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(999L);
    request.setWeight(10);

    when(memberService.getMember(999L))
        .thenThrow(
            new com.example.shinhangaecheokja.common.exception.EntityNotFoundException(
                com.example.shinhangaecheokja.common.exception.ErrorCode.MEMBER_NOT_FOUND));

    assertThatThrownBy(() -> deliveryService.requestDelivery(request))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }

  @Test
  void 무게가_0이하면_InvalidDeliveryWeightException을_던진다() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(1L);
    request.setWeight(-5);

    assertThatThrownBy(() -> deliveryService.requestDelivery(request))
        .isInstanceOf(InvalidDeliveryWeightException.class);
  }

  @Test
  void 출발지와_도착지_좌표가_같으면_InvalidDeliveryDistanceException을_던진다() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setCustomerId(1L);
    request.setWeight(10);
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(37.0);
    request.setDropoffLongitude(127.0);

    assertThatThrownBy(() -> deliveryService.requestDelivery(request))
        .isInstanceOf(InvalidDeliveryDistanceException.class);
  }

  @Test
  void 존재하지_않는_배송_요청을_조회하면_EntityNotFoundException을_던진다() {
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> deliveryService.getDeliveryRequest(1L))
        .isInstanceOf(com.example.shinhangaecheokja.common.exception.EntityNotFoundException.class);
  }

  @Test
  void 존재하는_배송_요청을_조회하면_DeliveryResponse를_반환한다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setCustomerId(1L);
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryResponse response = deliveryService.getDeliveryRequest(1L);

    assertThat(response.customerId()).isEqualTo(1L);
  }

  @Test
  void REQUESTED_상태면_주소를_수정한다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    deliveryRequest.setPickupAddress("서울시 강남구");
    deliveryRequest.setDropoffAddress("서울시 서초구");
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryUpdateRequest request = new DeliveryUpdateRequest();
    request.setPickupAddress("서울시 송파구");
    request.setDropoffAddress("서울시 강동구");

    DeliveryResponse response = deliveryService.updateDeliveryRequest(1L, request);

    assertThat(response.pickupAddress()).isEqualTo("서울시 송파구");
    assertThat(response.dropoffAddress()).isEqualTo("서울시 강동구");
  }

  @Test
  void REQUESTED가_아니면_주소_수정_시_AlreadyMatchedException을_던진다() {
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
  void 매칭이_없으면_배송_요청을_삭제한다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));
    when(matchingRepository.existsByDeliveryRequestId(1L)).thenReturn(false);

    deliveryService.deleteDeliveryRequest(1L);

    verify(deliveryRequestRepository).delete(deliveryRequest);
  }

  @Test
  void 매칭된_배송_요청을_삭제하려하면_AlreadyMatchedException을_던진다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));
    when(matchingRepository.existsByDeliveryRequestId(1L)).thenReturn(true);

    assertThatThrownBy(() -> deliveryService.deleteDeliveryRequest(1L))
        .isInstanceOf(AlreadyMatchedException.class);
    verify(deliveryRequestRepository, never()).delete(any(DeliveryRequest.class));
  }

  @Test
  void 출발지와_도착지_좌표가_같으면_거리할증_없이_기본료_무게할증_크기할증만_반환한다() {
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
  void 서울에서_부산까지_거리_무게_크기를_반영해_요금을_산정한다() {
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
  void 물품_크기가_SMALL이면_크기할증이_없다() {
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
  void 물품_크기가_LARGE이면_60퍼센트_할증이_붙는다() {
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
  void 견적_금액과_실제_배송_요청_금액이_동일한_입력에_대해_항상_일치한다() {
    DeliveryEstimateRequest estimateRequest = new DeliveryEstimateRequest();
    estimateRequest.setPickupLatitude(37.0);
    estimateRequest.setPickupLongitude(127.0);
    estimateRequest.setDestinationLatitude(38.0);
    estimateRequest.setDestinationLongitude(127.0);
    estimateRequest.setWeight(10.0);
    estimateRequest.setItemSize(ItemSize.MEDIUM);

    DeliveryCreateRequest createRequest = new DeliveryCreateRequest();
    createRequest.setCustomerId(1L);
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
    DeliveryResponse created = deliveryService.requestDelivery(createRequest);

    assertThat(created.feePoint()).isEqualTo(estimate.totalFee().longValueExact());
  }

  @Test
  void PICKED_UP_상태면_완료_처리하고_증거사진_URL을_저장한다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.PICKED_UP);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    DeliveryResponse response = deliveryService.completeDelivery(1L, request);

    assertThat(response.status()).isEqualTo(DeliveryStatus.COMPLETED);
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
  void PICKED_UP이_아니면_완료_처리_시_InvalidDeliveryTransitionException을_던진다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryCompleteRequest request = new DeliveryCompleteRequest();
    request.setProofPhotoUrl("https://example.com/proof.jpg");

    assertThatThrownBy(() -> deliveryService.completeDelivery(1L, request))
        .isInstanceOf(InvalidDeliveryTransitionException.class);
  }

  @Test
  void MATCHED_상태면_픽업_완료로_전이하고_픽업시각을_저장한다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    DeliveryResponse response = deliveryService.confirmPickup(1L);

    assertThat(response.status()).isEqualTo(DeliveryStatus.PICKED_UP);
    assertThat(deliveryRequest.getPickedUpAt()).isNotNull();
    verify(eventPublisher)
        .publishEvent(
            argThat(
                (DeliveryStatusChangedEvent event) ->
                    event.deliveryRequestId().equals(1L)
                        && event.status() == DeliveryStatus.PICKED_UP));
  }

  @Test
  void MATCHED가_아니면_픽업_처리_시_InvalidDeliveryTransitionException을_던진다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    assertThatThrownBy(() -> deliveryService.confirmPickup(1L))
        .isInstanceOf(InvalidDeliveryTransitionException.class);
  }

  @Test
  void 완료된_배송의_증거사진을_조회한다() {
    LocalDateTime completedAt = LocalDateTime.of(2026, 7, 31, 10, 0);
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);
    deliveryRequest.setProofPhotoUrl("https://example.com/proof.jpg");
    deliveryRequest.setCompletedAt(completedAt);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    ProofPhotoResponse response = deliveryService.getProofPhoto(1L);

    assertThat(response.proofPhotoUrl()).isEqualTo("https://example.com/proof.jpg");
    assertThat(response.completedAt()).isEqualTo(completedAt);
  }

  @Test
  void 완료되지_않은_배송의_증거사진_조회_시_ProofPhotoNotFoundException을_던진다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    assertThatThrownBy(() -> deliveryService.getProofPhoto(1L))
        .isInstanceOf(ProofPhotoNotFoundException.class);
  }

  @Test
  void 완료된_배송이어도_증거사진_URL이_없으면_ProofPhotoNotFoundException을_던진다() {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setStatus(DeliveryStatus.COMPLETED);
    when(deliveryRequestRepository.findById(1L)).thenReturn(Optional.of(deliveryRequest));

    assertThatThrownBy(() -> deliveryService.getProofPhoto(1L))
        .isInstanceOf(ProofPhotoNotFoundException.class);
  }
}

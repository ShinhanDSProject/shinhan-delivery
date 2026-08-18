package com.example.shinhandelivery.delivery.service;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryPayRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryPickupRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhandelivery.delivery.dto.response.DeliveryDetailResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryPaymentResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryReorderResponse;
import com.example.shinhandelivery.delivery.dto.response.DeliveryResponse;
import com.example.shinhandelivery.delivery.dto.response.PickupPhotoResponse;
import com.example.shinhandelivery.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.event.DeliveryOfferBroadcastEvent;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.exception.DeliveryAccessDeniedException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhandelivery.delivery.exception.PickupPhotoNotFoundException;
import com.example.shinhandelivery.delivery.exception.ProofPhotoNotFoundException;
import com.example.shinhandelivery.delivery.helper.DeliveryFeeCalculator;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.payment.dto.request.PointUseRequest;
import com.example.shinhandelivery.payment.dto.response.PointUseResultResponse;
import com.example.shinhandelivery.payment.service.PaymentService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** DeliveryRequest 관련 유스케이스(요청/조회/수정/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class DeliveryService {

  private final DeliveryRequestRepository deliveryRequestRepository;
  private final MemberService memberService;
  private final VehicleService vehicleService;
  private final MatchingRepository matchingRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final DeliveryFeeCalculator deliveryFeeCalculator;
  private final PaymentService paymentService;

  /**
   * 로그인한 고객 본인 명의로 배송을 요청한다. customerId는 요청 바디가 아니라 인증된 호출자에게서 받아, 다른 회원 명의로 배송을 대신 생성하지 못하게 한다.
   * 요금은 {@link #estimateFee}와 동일한 공식으로 서버가 직접 계산한다(거리는 클라이언트가 준 값이 아니라 좌표로 계산 — 요금 조작 방지). 매칭은 차량이
   * 콜을 수락하면서 별도로 이루어진다. 저장 직후, 조건(용량)이 맞는 후보 차량이 있으면 오퍼 브로드캐스트 이벤트를 발행한다.
   */
  @Transactional
  public DeliveryRequest requestDelivery(Long customerId, DeliveryCreateRequest request) {
    memberService.getById(customerId);

    double distanceKm =
        deliveryFeeCalculator.calculateDistanceKm(
            request.getPickupLocation(), request.getDropoffLocation());

    DeliveryEstimateResponse fee =
        deliveryFeeCalculator.calculateFee(distanceKm, request.getWeight(), request.getItemSize());

    DeliveryRequest saved =
        deliveryRequestRepository.save(
            DeliveryRequest.of(customerId, request, distanceKm, fee.totalFee().longValueExact()));
    publishOfferIfCandidatesExist(saved);
    return saved;
  }

  /** 배송 요청을 생성하지 않고 예상 요금만 계산한다({@link #requestDelivery}와 완전히 동일한 공식). */
  @Transactional(readOnly = true)
  public DeliveryEstimateResponse estimateFee(DeliveryEstimateRequest request) {
    double distanceKm =
        deliveryFeeCalculator.calculateDistanceKm(
            request.getPickupLocation(), request.getDestinationLocation());

    return deliveryFeeCalculator.calculateFee(
        distanceKm, request.getWeight(), request.getItemSize());
  }

  /** 배송 결제와 배송 요청 생성을 하나의 트랜잭션으로 처리한다. */
  @Transactional
  public DeliveryPaymentResponse payDelivery(
      Long customerId, String idempotencyKey, DeliveryPayRequest request) {
    memberService.getById(customerId);

    DeliveryEstimateResponse estimate = estimateFee(toEstimateRequest(request));
    DeliveryCreateRequest createRequest = toCreateRequest(request);
    double distanceKm =
        deliveryFeeCalculator.calculateDistanceKm(
            createRequest.getPickupLocation(), createRequest.getDropoffLocation());
    DeliveryRequest pendingDeliveryRequest =
        DeliveryRequest.of(
            customerId, createRequest, distanceKm, estimate.totalFee().longValueExact());

    PointUseRequest pointUseRequest = new PointUseRequest();
    pointUseRequest.setAmount(estimate.totalFee().longValueExact());
    PointUseResultResponse pointResult =
        paymentService.usePoint(customerId, idempotencyKey, pointUseRequest);

    DeliveryRequest deliveryRequest =
        deliveryRequestRepository
            .findByMemberIdAndPaymentIdempotencyKey(customerId, idempotencyKey)
            .orElseGet(
                () -> {
                  pendingDeliveryRequest.setPaymentIdempotencyKey(idempotencyKey);
                  DeliveryRequest saved = deliveryRequestRepository.save(pendingDeliveryRequest);
                  publishOfferIfCandidatesExist(saved);
                  return saved;
                });

    return DeliveryPaymentResponse.from(
        deliveryRequest, pointResult.usedAmount(), pointResult.balance());
  }

  /** id로 배송 요청 단건을 조회한다 (DeliveryRequest Entity 리턴). 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public DeliveryRequest getDeliveryRequest(Long deliveryRequestId) {
    return findDeliveryRequestOrThrow(deliveryRequestId);
  }

  /** 로그인 회원 본인의 배송 요청을 최신순으로 페이징 조회한다 (DeliveryRequest Entity 리턴). status가 있으면 그 상태로만 필터링한다. */
  @Transactional(readOnly = true)
  public Page<DeliveryRequest> getMyDeliveryRequests(
      Long customerId, DeliveryStatus status, Pageable pageable) {
    return status == null
        ? deliveryRequestRepository.findByMemberIdOrderByCreatedAtDescIdDesc(customerId, pageable)
        : deliveryRequestRepository.findByMemberIdAndStatusOrderByCreatedAtDescIdDesc(
            customerId, status, pageable);
  }

  /** 고객 본인의 과거 배송에서 신규 배송 초안에 다시 사용할 입력값만 조회한다. */
  @Transactional(readOnly = true)
  public DeliveryReorderResponse getReorderDraft(Long customerId, Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (!customerId.equals(deliveryRequest.getMemberId())) {
      throw new DeliveryAccessDeniedException(deliveryRequestId, customerId);
    }
    return DeliveryReorderResponse.from(deliveryRequest);
  }

  /**
   * 배송 요청 상세를 조회한다. 배송 요청의 고객 본인이거나 배정된 배송원 본인만 조회할 수 있고, 둘 다 아니면 DeliveryAccessDeniedException을
   * 던진다. 매칭된 배송원이 있으면 차량 소유자(Member)의 이름과 차량 종류를 courierName·vehicleType으로 함께 담고, 없으면 둘 다 null이다.
   */
  @Transactional(readOnly = true)
  public DeliveryDetailResponse getDeliveryRequestDetail(Long callerId, Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);

    Matching matching = matchingRepository.findByDeliveryRequestId(deliveryRequestId).orElse(null);
    Vehicle vehicle = matching == null ? null : vehicleService.getById(matching.getVehicleId());
    Long courierId = vehicle == null ? null : vehicle.getMemberId();
    assertCallerHasAccess(callerId, deliveryRequestId, deliveryRequest, courierId);

    String courierName = courierId == null ? null : memberService.getById(courierId).getName();
    LocalDateTime matchedAt = matching == null ? null : matching.getMatchedAt();
    VehicleType vehicleType = vehicle == null ? null : vehicle.getType();
    return DeliveryDetailResponse.from(deliveryRequest, courierName, matchedAt, vehicleType);
  }

  /**
   * 배송 요청의 픽업지·도착지를 수정한다. 고객·무게·거리·요금은 변경하지 않는다. 이미 콜을 수락한 차량이 있거나(MATCHED) 완료·취소된 배송 요청은 수정할 수
   * 없다(REQUESTED 상태에서만 허용). (DeliveryRequest Entity 리턴)
   */
  @Transactional
  public DeliveryRequest updateDeliveryRequest(
      Long deliveryRequestId, DeliveryUpdateRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.REQUESTED) {
      throw new AlreadyMatchedException(deliveryRequestId, deliveryRequest.getStatus());
    }
    return deliveryRequest.updateBy(request);
  }

  /**
   * id로 배송 요청을 조회해 삭제한다. 없으면 EntityNotFoundException. 연결된 매칭이 있으면 (콜을 수락했거나 완료·취소된 이력이 있으면) FK 제약
   * 위반으로 500이 나는 대신 AlreadyMatchedException을 던져 명확히 거절한다.
   */
  @Transactional
  public void deleteDeliveryRequest(Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (matchingRepository.existsByDeliveryRequestId(deliveryRequestId)) {
      throw new AlreadyMatchedException(deliveryRequestId, deliveryRequest.getStatus());
    }
    deliveryRequestRepository.delete(deliveryRequest);
  }

  /**
   * 배송원의 픽업 완료를 처리한다 (DeliveryRequest Entity 리턴). 배송원이 콜을 수락한(MATCHED) 배송 요청만 픽업 완료로 전이할 수 있고, 호출자가
   * 배정된 배송원 본인이 아니면 DeliveryAccessDeniedException을 던진다. 물품 확인 사진 URL을 같이 기록한다.
   */
  @Transactional
  public DeliveryRequest confirmPickup(
      Long callerId, Long deliveryRequestId, DeliveryPickupRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestForUpdateOrThrow(deliveryRequestId);
    assertCallerIsAssignedCourier(callerId, deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.MATCHED) {
      throw new InvalidDeliveryTransitionException(
          deliveryRequest.getStatus(), DeliveryStatus.PICKED_UP);
    }
    LocalDateTime pickedUpAt = LocalDateTime.now();
    deliveryRequest.pickUp(request.getPickupPhotoUrl(), pickedUpAt);
    eventPublisher.publishEvent(
        new DeliveryStatusChangedEvent(deliveryRequestId, DeliveryStatus.PICKED_UP, pickedUpAt));
    return deliveryRequest;
  }

  /**
   * 배송을 완료 처리한다 (DeliveryRequest Entity 리턴). 픽업을 완료한(PICKED_UP) 배송 요청만 완료할 수 있으며, 완료와 동시에 증거 사진
   * URL을 저장한다. 사진 파일 자체는 이 메서드 호출 전에 {@code POST /api/v1/uploads/image}로 이미 업로드되어 있어야 한다. 호출자가 배정된
   * 배송원 본인이 아니면 DeliveryAccessDeniedException을 던진다. 매칭된 차량은 다시 AVAILABLE로 되돌려 새 오퍼를 받을 수 있게 한다.
   */
  @Transactional
  public DeliveryRequest completeDelivery(
      Long callerId, Long deliveryRequestId, DeliveryCompleteRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestForUpdateOrThrow(deliveryRequestId);
    assertCallerIsAssignedCourier(callerId, deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.PICKED_UP) {
      throw new InvalidDeliveryTransitionException(
          deliveryRequest.getStatus(), DeliveryStatus.COMPLETED);
    }
    Matching matching =
        matchingRepository
            .findByDeliveryRequestIdForUpdate(deliveryRequestId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATCHING_NOT_FOUND));
    Vehicle vehicle = vehicleService.getVehicleForUpdate(matching.getVehicleId());
    vehicle.markAs(VehicleStatus.AVAILABLE);

    LocalDateTime completedAt = LocalDateTime.now();
    deliveryRequest.complete(request, completedAt);
    eventPublisher.publishEvent(
        new DeliveryStatusChangedEvent(deliveryRequestId, DeliveryStatus.COMPLETED, completedAt));
    return deliveryRequest;
  }

  /**
   * 완료된 배송 요청의 증거 사진을 조회한다. 배송 요청의 고객 본인이거나 배정된 배송원 본인만 조회할 수 있고, 둘 다 아니면
   * DeliveryAccessDeniedException을 던진다. 완료되지 않았거나 사진이 없으면 ProofPhotoNotFoundException.
   */
  @Transactional(readOnly = true)
  public ProofPhotoResponse getProofPhoto(Long callerId, Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);

    Matching matching = matchingRepository.findByDeliveryRequestId(deliveryRequestId).orElse(null);
    assertCallerHasAccess(callerId, deliveryRequestId, deliveryRequest, courierIdOf(matching));

    if (deliveryRequest.getStatus() != DeliveryStatus.COMPLETED
        || deliveryRequest.getProofPhotoUrl() == null) {
      throw new ProofPhotoNotFoundException(deliveryRequestId);
    }
    return ProofPhotoResponse.from(deliveryRequest);
  }

  /**
   * 픽업 완료된 배송 요청의 물품 확인 사진을 조회한다. 배송 요청의 고객 본인이거나 배정된 배송원 본인만 조회할 수 있고, 둘 다 아니면
   * DeliveryAccessDeniedException을 던진다. 픽업이 완료되지 않았거나 사진이 없으면 PickupPhotoNotFoundException.
   */
  @Transactional(readOnly = true)
  public PickupPhotoResponse getPickupPhoto(Long callerId, Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);

    Matching matching = matchingRepository.findByDeliveryRequestId(deliveryRequestId).orElse(null);
    assertCallerHasAccess(callerId, deliveryRequestId, deliveryRequest, courierIdOf(matching));

    boolean pickupNotDoneYet =
        deliveryRequest.getStatus() != DeliveryStatus.PICKED_UP
            && deliveryRequest.getStatus() != DeliveryStatus.COMPLETED;
    if (pickupNotDoneYet || deliveryRequest.getPickupPhotoUrl() == null) {
      throw new PickupPhotoNotFoundException(deliveryRequestId);
    }
    return PickupPhotoResponse.from(deliveryRequest);
  }

  /** 매칭이 있으면 그 차량 소유주(Member) id를, 매칭이 없으면(아직 아무도 안 맡았으면) null을 반환한다. */
  /**
   * 엔티티 전체가 아니라 소유주(memberId)만 프로젝션으로 조회한다 — completeDelivery처럼 뒤이어 같은 차량에 비관적 락을 거는 호출 경로가 있어서,
   * Vehicle 엔티티를 먼저 캐시에 올리지 않기 위함이다({@link VehicleService#getOwnerMemberId} 참고).
   */
  private Long courierIdOf(Matching matching) {
    return matching == null ? null : vehicleService.getOwnerMemberId(matching.getVehicleId());
  }

  /**
   * 호출자가 배송 요청에 배정된 배송원 본인이 아니면 DeliveryAccessDeniedException을 던진다. 픽업·완료 처리는 고객이 아니라 배정된 배송원만 할 수
   * 있다(조회와 달리 고객 본인은 허용하지 않음 — {@link #assertCallerHasAccess}와 다른 점).
   */
  private void assertCallerIsAssignedCourier(Long callerId, Long deliveryRequestId) {
    Matching matching = matchingRepository.findByDeliveryRequestId(deliveryRequestId).orElse(null);
    Long courierId = courierIdOf(matching);
    if (!callerId.equals(courierId)) {
      throw new DeliveryAccessDeniedException(deliveryRequestId, callerId);
    }
  }

  /** 호출자가 배송 요청의 고객 본인이거나 배정된 배송원 본인이 아니면 DeliveryAccessDeniedException을 던진다. */
  private void assertCallerHasAccess(
      Long callerId, Long deliveryRequestId, DeliveryRequest deliveryRequest, Long courierId) {
    boolean isOwner = callerId.equals(deliveryRequest.getMemberId());
    boolean isAssignedCourier = callerId.equals(courierId);
    if (!isOwner && !isAssignedCourier) {
      throw new DeliveryAccessDeniedException(deliveryRequestId, callerId);
    }
  }

  private DeliveryRequest findDeliveryRequestOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findById(deliveryRequestId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }

  /** 픽업/완료 처리처럼 동시에 두 번 눌려도 하나만 성공해야 하는 상태 전이 전에, 비관적 쓰기 락으로 조회한다. */
  private DeliveryRequest findDeliveryRequestForUpdateOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findByIdForUpdate(deliveryRequestId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }

  private DeliveryEstimateRequest toEstimateRequest(DeliveryPayRequest request) {
    DeliveryEstimateRequest estimateRequest = new DeliveryEstimateRequest();
    estimateRequest.setPickupLatitude(request.getPickup().getLat());
    estimateRequest.setPickupLongitude(request.getPickup().getLng());
    estimateRequest.setDestinationLatitude(request.getDropoff().getLat());
    estimateRequest.setDestinationLongitude(request.getDropoff().getLng());
    estimateRequest.setWeight(request.getWeight());
    estimateRequest.setItemSize(request.getItemSize());
    return estimateRequest;
  }

  private DeliveryCreateRequest toCreateRequest(DeliveryPayRequest request) {
    DeliveryCreateRequest createRequest = new DeliveryCreateRequest();
    createRequest.setPickupAddress(request.getPickup().getAddress());
    createRequest.setDropoffAddress(request.getDropoff().getAddress());
    createRequest.setPickupLatitude(request.getPickup().getLat());
    createRequest.setPickupLongitude(request.getPickup().getLng());
    createRequest.setDropoffLatitude(request.getDropoff().getLat());
    createRequest.setDropoffLongitude(request.getDropoff().getLng());
    createRequest.setWeight(request.getWeight());
    createRequest.setItemSize(request.getItemSize());
    createRequest.setDeliveryInstructionType(request.getDeliveryInstructionType());
    createRequest.setEntranceCode(request.getEntranceCode());
    createRequest.setUnitDetail(request.getUnitDetail());
    createRequest.setDeliveryNote(request.getDeliveryNote());
    createRequest.setDeliveryReferencePhotoUrl(request.getDeliveryReferencePhotoUrl());
    return createRequest;
  }

  private void publishOfferIfCandidatesExist(DeliveryRequest deliveryRequest) {
    List<Long> candidateVehicleIds =
        vehicleService
            .findOfferCandidates(
                deliveryRequest.getWeight(),
                deliveryRequest.getDistance(),
                deliveryRequest.getPickupLocation())
            .stream()
            .map(Vehicle::getId)
            .toList();
    if (!candidateVehicleIds.isEmpty()) {
      eventPublisher.publishEvent(
          new DeliveryOfferBroadcastEvent(
              candidateVehicleIds, DeliveryResponse.from(deliveryRequest)));
    }
  }
}

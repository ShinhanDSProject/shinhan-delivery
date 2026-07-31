package com.example.shinhangaecheokja.delivery.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryEstimateResponse;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** DeliveryRequest 관련 유스케이스(요청/조회/수정/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class DeliveryService {

  private static final double EARTH_RADIUS_KM = 6371.0;
  private static final BigDecimal ESTIMATE_BASE_FEE = BigDecimal.valueOf(3000);
  private static final BigDecimal ESTIMATE_FEE_PER_KM = BigDecimal.valueOf(500);
  private static final BigDecimal ESTIMATE_FEE_PER_KG = BigDecimal.valueOf(200);
  private static final BigDecimal SIZE_SURCHARGE_RATE_MEDIUM = BigDecimal.valueOf(0.30);
  private static final BigDecimal SIZE_SURCHARGE_RATE_LARGE = BigDecimal.valueOf(0.60);

  private final DeliveryRequestRepository deliveryRequestRepository;
  private final MemberService memberService;
  private final MatchingRepository matchingRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 고객 존재 여부를 검증한 뒤 배송을 요청한다. 요금은 {@link #estimateFee}와 동일한 공식으로 서버가 직접 계산한다(거리는 클라이언트가 준 값이 아니라
   * 좌표로 계산 — 요금 조작 방지). 매칭은 차량이 콜을 수락하면서 별도로 이루어진다.
   */
  @Transactional
  public DeliveryRequest requestDelivery(DeliveryCreateRequest request) {
    memberService.getMember(request.getCustomerId());
    validateWeight(request.getWeight());

    double distanceKm =
        calculateHaversineDistance(
            request.getPickupLatitude(),
            request.getPickupLongitude(),
            request.getDropoffLatitude(),
            request.getDropoffLongitude());
    validateDistance(distanceKm);

    DeliveryEstimateResponse fee =
        calculateFee(distanceKm, request.getWeight(), request.getItemSize());

    DeliveryRequest deliveryRequest =
        DeliveryRequest.of(request, distanceKm, fee.totalFee().longValueExact());

    return deliveryRequestRepository.save(deliveryRequest);
  }

  /** 배송 요청을 생성하지 않고 예상 요금만 계산한다({@link #requestDelivery}와 완전히 동일한 공식). */
  @Transactional(readOnly = true)
  public DeliveryEstimateResponse estimateFee(DeliveryEstimateRequest request) {
    double distanceKm =
        calculateHaversineDistance(
            request.getPickupLatitude(),
            request.getPickupLongitude(),
            request.getDestinationLatitude(),
            request.getDestinationLongitude());

    return calculateFee(distanceKm, request.getWeight(), request.getItemSize());
  }

  /**
   * 기본료 + 거리 할증(하버사인 거리 × km당 요금) + 무게 할증(무게 × kg당 요금)의 소계에, 물품 크기별 할증률(SMALL 0%/MEDIUM 30%/LARGE
   * 60%)을 곱한 크기 할증을 더한다. {@link #requestDelivery}와 {@link #estimateFee}가 공유하는 단일 요금 공식이다.
   */
  private DeliveryEstimateResponse calculateFee(
      double distanceKm, double weight, ItemSize itemSize) {
    BigDecimal distanceSurcharge =
        ESTIMATE_FEE_PER_KM
            .multiply(BigDecimal.valueOf(distanceKm))
            .setScale(0, RoundingMode.HALF_UP);
    BigDecimal weightSurcharge =
        ESTIMATE_FEE_PER_KG.multiply(BigDecimal.valueOf(weight)).setScale(0, RoundingMode.HALF_UP);
    BigDecimal subtotal = ESTIMATE_BASE_FEE.add(distanceSurcharge).add(weightSurcharge);
    BigDecimal sizeSurcharge =
        subtotal.multiply(sizeSurchargeRate(itemSize)).setScale(0, RoundingMode.HALF_UP);
    BigDecimal totalFee = subtotal.add(sizeSurcharge);

    return new DeliveryEstimateResponse(
        ESTIMATE_BASE_FEE, distanceSurcharge, weightSurcharge, sizeSurcharge, totalFee);
  }

  private BigDecimal sizeSurchargeRate(ItemSize itemSize) {
    return switch (itemSize) {
      case SMALL -> BigDecimal.ZERO;
      case MEDIUM -> SIZE_SURCHARGE_RATE_MEDIUM;
      case LARGE -> SIZE_SURCHARGE_RATE_LARGE;
    };
  }

  /** 두 좌표(위도/경도) 간의 대권 거리를 하버사인 공식으로 계산한다(단위: km). */
  private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }

  /** id로 배송 요청 단건을 조회한다 (DeliveryRequest Entity 리턴). 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public DeliveryRequest getDeliveryRequest(Long deliveryRequestId) {
    return findDeliveryRequestOrThrow(deliveryRequestId);
  }

  /** 전체 배송 요청 목록을 조회한다 (DeliveryRequest Entity 리턴). */
  @Transactional(readOnly = true)
  public List<DeliveryRequest> getDeliveryRequests() {
    return deliveryRequestRepository.findAll();
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

  /** 배송원의 픽업 완료를 처리한다 (DeliveryRequest Entity 리턴). 배송원이 콜을 수락한(MATCHED) 배송 요청만 픽업 완료로 전이할 수 있다. */
  @Transactional
  public DeliveryRequest confirmPickup(Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestForUpdateOrThrow(deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.MATCHED) {
      throw new InvalidDeliveryTransitionException(
          deliveryRequest.getStatus(), DeliveryStatus.PICKED_UP);
    }
    LocalDateTime pickedUpAt = LocalDateTime.now();
    deliveryRequest.pickUp(pickedUpAt);
    eventPublisher.publishEvent(
        new DeliveryStatusChangedEvent(deliveryRequestId, DeliveryStatus.PICKED_UP, pickedUpAt));
    return deliveryRequest;
  }

  /**
   * 배송을 완료 처리한다 (DeliveryRequest Entity 리턴). 픽업을 완료한(PICKED_UP) 배송 요청만 완료할 수 있으며, 완료와 동시에 증거 사진
   * URL을 저장한다. 사진 파일 자체는 이 메서드 호출 전에 {@code POST /api/v1/uploads/image}로 이미 업로드되어 있어야 한다.
   */
  @Transactional
  public DeliveryRequest completeDelivery(Long deliveryRequestId, DeliveryCompleteRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestForUpdateOrThrow(deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.PICKED_UP) {
      throw new InvalidDeliveryTransitionException(
          deliveryRequest.getStatus(), DeliveryStatus.COMPLETED);
    }
    LocalDateTime completedAt = LocalDateTime.now();
    deliveryRequest.complete(request, completedAt);
    eventPublisher.publishEvent(
        new DeliveryStatusChangedEvent(deliveryRequestId, DeliveryStatus.COMPLETED, completedAt));
    return deliveryRequest;
  }

  /** 완료된 배송 요청의 증거 사진을 조회한다. 완료되지 않았거나 사진이 없으면 ProofPhotoNotFoundException. */
  @Transactional(readOnly = true)
  public ProofPhotoResponse getProofPhoto(Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.COMPLETED
        || deliveryRequest.getProofPhotoUrl() == null) {
      throw new ProofPhotoNotFoundException(deliveryRequestId);
    }
    return ProofPhotoResponse.from(deliveryRequest);
  }

  private void validateWeight(double weight) {
    if (weight <= 0) {
      throw new InvalidDeliveryWeightException(weight);
    }
  }

  /** 출발지·도착지 좌표가 같으면(거리 0) 유효하지 않은 배송 요청으로 거절한다. */
  private void validateDistance(double distanceKm) {
    if (distanceKm <= 0) {
      throw new InvalidDeliveryDistanceException(distanceKm);
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
}

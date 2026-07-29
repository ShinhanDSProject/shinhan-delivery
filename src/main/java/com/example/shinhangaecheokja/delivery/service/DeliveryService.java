package com.example.shinhangaecheokja.delivery.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.ItemSize;
import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryDistanceException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryWeightException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.member.service.MemberService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

  /**
   * 고객 존재 여부를 검증한 뒤 배송을 요청한다. 요금은 {@link #estimateFee}와 동일한 공식으로 서버가 직접 계산한다(거리는 클라이언트가 준 값이 아니라
   * 좌표로 계산 — 요금 조작 방지). 매칭은 차량이 콜을 수락하면서 별도로 이루어진다.
   */
  @Transactional
  public DeliveryResponse requestDelivery(DeliveryCreateRequest request) {
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

    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setCustomerId(request.getCustomerId());
    deliveryRequest.setPickupAddress(request.getPickupAddress());
    deliveryRequest.setDropoffAddress(request.getDropoffAddress());
    deliveryRequest.setWeight(request.getWeight());
    deliveryRequest.setDistance(distanceKm);
    deliveryRequest.setPickupLatitude(request.getPickupLatitude());
    deliveryRequest.setPickupLongitude(request.getPickupLongitude());
    deliveryRequest.setDropoffLatitude(request.getDropoffLatitude());
    deliveryRequest.setDropoffLongitude(request.getDropoffLongitude());
    deliveryRequest.setItemSize(request.getItemSize());
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    // calculateFee()가 매 단계 setScale(0, HALF_UP)을 거치므로 totalFee는 항상 정수다. longValueExact()를 쓰는
    // 이유는, 만약 나중에 요금 정책이 바뀌어 이 불변조건이 깨지면 소수점을 조용히 버리지 않고 즉시 예외로 드러내기 위함이다.
    deliveryRequest.setFeePoint(fee.totalFee().longValueExact());

    DeliveryRequest saved = deliveryRequestRepository.save(deliveryRequest);

    return DeliveryResponse.from(saved);
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

  /** id로 배송 요청 단건을 조회한다. 없으면 EntityNotFoundException. */
  @Transactional(readOnly = true)
  public DeliveryResponse getDeliveryRequest(Long deliveryRequestId) {
    return DeliveryResponse.from(findDeliveryRequestOrThrow(deliveryRequestId));
  }

  /** 전체 배송 요청 목록을 조회한다. */
  @Transactional(readOnly = true)
  public List<DeliveryResponse> getDeliveryRequests() {
    return deliveryRequestRepository.findAll().stream().map(DeliveryResponse::from).toList();
  }

  /**
   * 배송 요청의 픽업지·도착지를 수정한다. 고객·무게·거리·요금은 변경하지 않는다. 이미 콜을 수락한 차량이 있거나(MATCHED) 완료·취소된 배송 요청은 수정할 수
   * 없다(REQUESTED 상태에서만 허용).
   */
  @Transactional
  public DeliveryResponse updateDeliveryRequest(
      Long deliveryRequestId, DeliveryUpdateRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.REQUESTED) {
      throw new AlreadyMatchedException(deliveryRequestId, deliveryRequest.getStatus());
    }
    deliveryRequest.setPickupAddress(request.getPickupAddress());
    deliveryRequest.setDropoffAddress(request.getDropoffAddress());
    return DeliveryResponse.from(deliveryRequest);
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
}

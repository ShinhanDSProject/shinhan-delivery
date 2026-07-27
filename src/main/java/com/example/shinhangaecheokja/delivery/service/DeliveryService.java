package com.example.shinhangaecheokja.delivery.service;

import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.exception.DeliveryRequestNotFoundException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryDistanceException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryWeightException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.member.service.MemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** DeliveryRequest 관련 유스케이스(요청/조회/수정/삭제)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class DeliveryService {

  private static final long FEE_PER_DISTANCE = 100L;
  private static final long FEE_PER_WEIGHT = 10L;

  private final DeliveryRequestRepository deliveryRequestRepository;
  private final MemberService memberService;
  private final MatchingRepository matchingRepository;

  /** 고객 존재 여부를 검증한 뒤 배송을 요청한다. 매칭은 차량이 콜을 수락하면서 별도로 이루어진다. */
  @Transactional
  public DeliveryResponse requestDelivery(DeliveryCreateRequest request) {
    memberService.getMember(request.getCustomerId());
    validateWeightAndDistance(request.getWeight(), request.getDistance());

    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setCustomerId(request.getCustomerId());
    deliveryRequest.setPickupAddress(request.getPickupAddress());
    deliveryRequest.setDropoffAddress(request.getDropoffAddress());
    deliveryRequest.setWeight(request.getWeight());
    deliveryRequest.setDistance(request.getDistance());
    deliveryRequest.setPickupLatitude(request.getPickupLatitude());
    deliveryRequest.setPickupLongitude(request.getPickupLongitude());
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    deliveryRequest.setFeePoint(calculateFee(request.getWeight(), request.getDistance()));

    DeliveryRequest saved = deliveryRequestRepository.save(deliveryRequest);

    return DeliveryResponse.from(saved);
  }

  /** id로 배송 요청 단건을 조회한다. 없으면 DeliveryRequestNotFoundException. */
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
   * 배송 요청의 픽업지·도착지를 수정한다. 고객·무게·거리·요금은 변경하지 않는다. 이미 콜을 수락한
   * 차량이 있거나(MATCHED) 완료·취소된 배송 요청은 수정할 수 없다(REQUESTED 상태에서만 허용).
   */
  @Transactional
  public DeliveryResponse updateDeliveryRequest(Long deliveryRequestId, DeliveryUpdateRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.REQUESTED) {
      throw new AlreadyMatchedException(deliveryRequestId, deliveryRequest.getStatus());
    }
    deliveryRequest.setPickupAddress(request.getPickupAddress());
    deliveryRequest.setDropoffAddress(request.getDropoffAddress());
    return DeliveryResponse.from(deliveryRequest);
  }

  /**
   * id로 배송 요청을 조회해 삭제한다. 없으면 DeliveryRequestNotFoundException. 연결된 매칭이 있으면
   * (콜을 수락했거나 완료·취소된 이력이 있으면) FK 제약 위반으로 500이 나는 대신 AlreadyMatchedException을
   * 던져 명확히 거절한다.
   */
  @Transactional
  public void deleteDeliveryRequest(Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (matchingRepository.existsByDeliveryRequestId(deliveryRequestId)) {
      throw new AlreadyMatchedException(deliveryRequestId, deliveryRequest.getStatus());
    }
    deliveryRequestRepository.delete(deliveryRequest);
  }

  private void validateWeightAndDistance(double weight, double distance) {
    if (weight <= 0) {
      throw new InvalidDeliveryWeightException(weight);
    }
    if (distance <= 0) {
      throw new InvalidDeliveryDistanceException(distance);
    }
  }

  private long calculateFee(double weight, double distance) {
    return Math.round(distance * FEE_PER_DISTANCE + weight * FEE_PER_WEIGHT);
  }

  private DeliveryRequest findDeliveryRequestOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findById(deliveryRequestId)
        .orElseThrow(() -> new DeliveryRequestNotFoundException(deliveryRequestId));
  }
}

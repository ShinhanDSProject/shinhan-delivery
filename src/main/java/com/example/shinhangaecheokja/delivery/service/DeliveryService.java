package com.example.shinhangaecheokja.delivery.service;

import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.exception.DeliveryRequestNotFoundException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryDistanceException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryWeightException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
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
  private final MatchingService matchingService;

  /** 고객 존재 여부를 검증한 뒤 배송을 요청하고, 위치·무게·거리 조건에 맞는 배송원을 자동으로 매칭한다. */
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
    matchingService.autoMatch(saved);

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

  /** 배송 요청의 픽업지·도착지를 수정한다. 고객·무게·거리·요금은 변경하지 않는다. */
  @Transactional
  public DeliveryResponse updateDeliveryRequest(Long deliveryRequestId, DeliveryUpdateRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    deliveryRequest.setPickupAddress(request.getPickupAddress());
    deliveryRequest.setDropoffAddress(request.getDropoffAddress());
    return DeliveryResponse.from(deliveryRequest);
  }

  /** id로 배송 요청을 조회해 삭제한다. 없으면 DeliveryRequestNotFoundException. */
  @Transactional
  public void deleteDeliveryRequest(Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
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

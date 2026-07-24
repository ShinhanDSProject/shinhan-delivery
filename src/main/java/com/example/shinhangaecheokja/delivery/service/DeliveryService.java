package com.example.shinhangaecheokja.service;

import com.example.shinhangaecheokja.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.entity.DeliveryRequest;
import com.example.shinhangaecheokja.entity.DeliveryStatus;
import com.example.shinhangaecheokja.exception.DeliveryRequestNotFoundException;
import com.example.shinhangaecheokja.exception.MemberNotFoundException;
import com.example.shinhangaecheokja.exception.NoAvailableCourierException;
import com.example.shinhangaecheokja.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.repository.MemberRepository;
import com.example.shinhangaecheokja.repository.VehicleRepository;
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
  private final MemberRepository memberRepository;
  private final VehicleRepository vehicleRepository;

  /** 고객 존재 여부와 감당 가능한 차량 존재 여부를 검증한 뒤 배송을 요청한다. */
  @Transactional
  public DeliveryResponse requestDelivery(DeliveryCreateRequest request) {
    if (!memberRepository.existsById(request.getCustomerId())) {
      throw new MemberNotFoundException(request.getCustomerId());
    }
    if (!vehicleRepository.existsByMaxWeightGreaterThanEqualAndMaxDistanceGreaterThanEqual(
        request.getWeight(), request.getDistance())) {
      throw new NoAvailableCourierException(request.getWeight(), request.getDistance());
    }

    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setCustomerId(request.getCustomerId());
    deliveryRequest.setPickupAddress(request.getPickupAddress());
    deliveryRequest.setDropoffAddress(request.getDropoffAddress());
    deliveryRequest.setWeight(request.getWeight());
    deliveryRequest.setDistance(request.getDistance());
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    deliveryRequest.setFeePoint(calculateFee(request.getWeight(), request.getDistance()));

    return DeliveryResponse.from(deliveryRequestRepository.save(deliveryRequest));
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

  private long calculateFee(double weight, double distance) {
    return Math.round(distance * FEE_PER_DISTANCE + weight * FEE_PER_WEIGHT);
  }

  private DeliveryRequest findDeliveryRequestOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findById(deliveryRequestId)
        .orElseThrow(() -> new DeliveryRequestNotFoundException(deliveryRequestId));
  }
}

package com.example.shinhangaecheokja.delivery.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.exception.NoAvailableCourierException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.member.service.MemberService;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
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
  private final VehicleService vehicleService;

  /** 고객 존재 여부와 감당 가능한 차량 존재 여부를 검증한 뒤 배송을 요청한다. */
  @Transactional
  public DeliveryResponse requestDelivery(DeliveryCreateRequest request) {
    memberService.getMember(request.getCustomerId());
    if (!vehicleService.existsAvailableVehicle(request.getWeight(), request.getDistance())) {
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

  /** 배송 요청의 픽업지·도착지를 수정한다. 고객·무게·거리·요금은 변경하지 않는다. */
  @Transactional
  public DeliveryResponse updateDeliveryRequest(
      Long deliveryRequestId, DeliveryUpdateRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    deliveryRequest.setPickupAddress(request.getPickupAddress());
    deliveryRequest.setDropoffAddress(request.getDropoffAddress());
    return DeliveryResponse.from(deliveryRequest);
  }

  /** id로 배송 요청을 조회해 삭제한다. 없으면 EntityNotFoundException. */
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
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }
}

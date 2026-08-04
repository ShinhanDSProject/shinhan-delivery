package com.example.shinhandelivery.delivery.service;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryEstimateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhandelivery.delivery.dto.response.DeliveryDetailResponseDto;
import com.example.shinhandelivery.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhandelivery.delivery.dto.response.ProofPhotoResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.exception.DeliveryAccessDeniedException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhandelivery.delivery.exception.ProofPhotoNotFoundException;
import com.example.shinhandelivery.delivery.helper.DeliveryFeeCalculator;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** DeliveryRequest 관련 유스케이스를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class DeliveryService {

  private final DeliveryRequestRepository deliveryRequestRepository;
  private final MemberService memberService;
  private final VehicleService vehicleService;
  private final MatchingRepository matchingRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final DeliveryFeeCalculator deliveryFeeCalculator;

  @Transactional
  public DeliveryRequest requestDelivery(DeliveryCreateRequest request) {
    memberService.getById(request.getCustomerId());

    double distanceKm =
        deliveryFeeCalculator.calculateDistanceKm(
            request.getPickupLatitude(),
            request.getPickupLongitude(),
            request.getDropoffLatitude(),
            request.getDropoffLongitude());

    DeliveryEstimateResponse fee =
        deliveryFeeCalculator.calculateFee(distanceKm, request.getWeight(), request.getItemSize());

    return deliveryRequestRepository.save(
        DeliveryRequest.of(request, distanceKm, fee.totalFee().longValueExact()));
  }

  @Transactional(readOnly = true)
  public DeliveryEstimateResponse estimateFee(DeliveryEstimateRequest request) {
    double distanceKm =
        deliveryFeeCalculator.calculateDistanceKm(
            request.getPickupLatitude(),
            request.getPickupLongitude(),
            request.getDestinationLatitude(),
            request.getDestinationLongitude());

    return deliveryFeeCalculator.calculateFee(
        distanceKm, request.getWeight(), request.getItemSize());
  }

  @Transactional(readOnly = true)
  public DeliveryRequest getDeliveryRequest(Long deliveryRequestId) {
    return findDeliveryRequestOrThrow(deliveryRequestId);
  }

  @Transactional(readOnly = true)
  public Page<DeliveryRequest> getMyDeliveryRequests(
      Long customerId, DeliveryStatus status, Pageable pageable) {
    return status == null
        ? deliveryRequestRepository.findByCustomerIdOrderByCreatedAtDescIdDesc(customerId, pageable)
        : deliveryRequestRepository.findByCustomerIdAndStatusOrderByCreatedAtDescIdDesc(
            customerId, status, pageable);
  }

  @Transactional(readOnly = true)
  public DeliveryDetailResponseDto getDeliveryRequestDetail(Long callerId, Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);

    Long courierId =
        matchingRepository
            .findByDeliveryRequestId(deliveryRequestId)
            .map(matching -> vehicleService.getById(matching.getVehicleId()).getOwnerId())
            .orElse(null);

    boolean isOwner = callerId.equals(deliveryRequest.getCustomerId());
    boolean isAssignedCourier = callerId.equals(courierId);
    if (!isOwner && !isAssignedCourier) {
      throw new DeliveryAccessDeniedException(deliveryRequestId, callerId);
    }

    String courierName = courierId == null ? null : memberService.getById(courierId).getName();
    return DeliveryDetailResponseDto.from(deliveryRequest, courierName);
  }

  @Transactional
  public DeliveryRequest updateDeliveryRequest(
      Long deliveryRequestId, DeliveryUpdateRequest request) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.REQUESTED) {
      throw new AlreadyMatchedException(deliveryRequestId, deliveryRequest.getStatus());
    }
    return deliveryRequest.updateBy(request);
  }

  @Transactional
  public void deleteDeliveryRequest(Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (matchingRepository.existsByDeliveryRequestId(deliveryRequestId)) {
      throw new AlreadyMatchedException(deliveryRequestId, deliveryRequest.getStatus());
    }
    deliveryRequestRepository.delete(deliveryRequest);
  }

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

  @Transactional(readOnly = true)
  public ProofPhotoResponse getProofPhoto(Long deliveryRequestId) {
    DeliveryRequest deliveryRequest = findDeliveryRequestOrThrow(deliveryRequestId);
    if (deliveryRequest.getStatus() != DeliveryStatus.COMPLETED
        || deliveryRequest.getProofPhotoUrl() == null) {
      throw new ProofPhotoNotFoundException(deliveryRequestId);
    }
    return ProofPhotoResponse.from(deliveryRequest);
  }

  private DeliveryRequest findDeliveryRequestOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findById(deliveryRequestId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }

  private DeliveryRequest findDeliveryRequestForUpdateOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findByIdForUpdate(deliveryRequestId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }
}

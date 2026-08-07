package com.example.shinhandelivery.delivery.service;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.response.AvailableDeliveryResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.helper.DeliveryFeeCalculator;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 배송원의 주변 대기열(주문 목록) 조회 및 동시성 수락(Catch)을 전담하는 서비스. */
@Service
@RequiredArgsConstructor
public class DeliveryMatchingService {

  private final MemberService memberService;
  private final VehicleService vehicleService;
  private final DeliveryRequestRepository deliveryRequestRepository;
  private final MatchingRepository matchingRepository;
  private final DeliveryFeeCalculator feeCalculator;

  /** 배송원의 위치(GPS) 기준 반경 3km 이내의 대기 중인(REQUESTED) 주문 목록을 거리가 가까운 순으로 조회한다. */
  @Transactional(readOnly = true)
  public List<AvailableDeliveryResponse> getAvailableDeliveries(
      Long memberId, Double latitude, Double longitude, Double radiusKm) {
    Member member = memberService.getById(memberId);

    if (member.getRole() != MemberRole.COURIER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED, "배송원(COURIER)만 대기열 목록을 조회할 수 있습니다.");
    }

    double currentLat = latitude != null ? latitude : 0.0;
    double currentLon = longitude != null ? longitude : 0.0;
    double searchRadius = radiusKm != null ? radiusKm : 3.0;

    if (latitude == null || longitude == null) {
      List<Vehicle> vehicles = vehicleService.getVehiclesByMemberId(memberId);
      if (!vehicles.isEmpty()) {
        currentLat = vehicles.get(0).getLatitude();
        currentLon = vehicles.get(0).getLongitude();
      }
    }

    final double refLat = currentLat;
    final double refLon = currentLon;

    List<DeliveryRequest> requestedDeliveries =
        deliveryRequestRepository.findAllByStatus(DeliveryStatus.REQUESTED);

    Location courierLocation = Location.of(refLat, refLon);

    return requestedDeliveries.stream()
        .map(
            d -> {
              double distanceToPickup =
                  feeCalculator.calculateDistanceKm(courierLocation, d.getPickupLocation());
              return AvailableDeliveryResponse.builder()
                  .deliveryRequestId(d.getId())
                  .pickupAddress(d.getPickupAddress())
                  .pickupLatitude(d.getPickupLatitude())
                  .pickupLongitude(d.getPickupLongitude())
                  .dropoffAddress(d.getDropoffAddress())
                  .dropoffLatitude(d.getDropoffLatitude())
                  .dropoffLongitude(d.getDropoffLongitude())
                  .weight(d.getWeight())
                  .distanceKm(d.getDistance())
                  .distanceToPickupKm(distanceToPickup)
                  .feePoint(d.getFeePoint())
                  .status(d.getStatus())
                  .itemSize(d.getItemSize())
                  .createdAt(d.getCreatedAt())
                  .build();
            })
        .filter(dto -> dto.getDistanceToPickupKm() <= searchRadius)
        .sorted(Comparator.comparingDouble(AvailableDeliveryResponse::getDistanceToPickupKm))
        .toList();
  }

  /**
   * 특정 대기 주문을 배송원이 수락(Catch)한다. 동시 접속 환경에서 낙관적 락(@Version)으로 단 1명만 수락 성공을 보장하며, 경합 실패 시
   * AlreadyMatchedException(409 Conflict)을 던진다.
   */
  @Transactional
  public Matching catchDelivery(Long memberId, Long deliveryRequestId) {
    Member member = memberService.getById(memberId);

    if (member.getRole() != MemberRole.COURIER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED, "배송원(COURIER)만 주문을 수락할 수 있습니다.");
    }

    List<Vehicle> vehicles = vehicleService.getVehiclesByMemberId(memberId);
    if (vehicles.isEmpty()) {
      throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND, "등록된 운송수단이 없어 주문을 수락할 수 없습니다.");
    }
    Vehicle vehicle = vehicles.get(0);

    if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
      throw new BusinessException(
          ErrorCode.VEHICLE_NOT_AVAILABLE, "영업 중(ONLINE) 상태에서만 주문을 수락할 수 있습니다.");
    }

    try {
      DeliveryRequest deliveryRequest =
          deliveryRequestRepository
              .findByIdForUpdate(deliveryRequestId)
              .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));

      if (deliveryRequest.getStatus() != DeliveryStatus.REQUESTED) {
        throw new AlreadyMatchedException(deliveryRequestId, deliveryRequest.getStatus());
      }

      deliveryRequest.setStatus(DeliveryStatus.MATCHED);
      deliveryRequestRepository.saveAndFlush(deliveryRequest);

      Matching matching = Matching.of(deliveryRequestId, vehicle.getId());

      return matchingRepository.saveAndFlush(matching);

    } catch (DataIntegrityViolationException e) {
      throw new AlreadyMatchedException(deliveryRequestId, DeliveryStatus.MATCHED);
    }
  }
}

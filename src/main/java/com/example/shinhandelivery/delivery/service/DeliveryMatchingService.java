package com.example.shinhandelivery.delivery.service;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.response.AvailableDeliveryResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.exception.VehicleCapacityMismatchException;
import com.example.shinhandelivery.delivery.helper.DeliveryFeeCalculator;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.service.MemberService;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 배송원의 위치(GPS) 기준 반경 3km 이내의 대기 중인(REQUESTED) 주문 중, 이 배송원의 차량이 감당할 수 있는(무게·거리 용량 이내) 것만 거리가 가까운
   * 순으로 조회한다. 용량 필터는 DB 쿼리 시점에 걸러 불필요한 행을 아예 가져오지 않는다.
   */
  @Transactional(readOnly = true)
  public List<AvailableDeliveryResponse> getAvailableDeliveries(
      Long memberId, Double latitude, Double longitude, Double radiusKm) {
    Member member = memberService.getById(memberId);

    if (member.getRole() != MemberRole.COURIER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED, "배송원(COURIER)만 대기열 목록을 조회할 수 있습니다.");
    }

    Vehicle vehicle = vehicleService.getActiveVehicle(memberId);

    double currentLat = latitude != null ? latitude : vehicle.getLatitude();
    double currentLon = longitude != null ? longitude : vehicle.getLongitude();
    double searchRadius = radiusKm != null ? radiusKm : VehicleService.DEFAULT_OFFER_RADIUS_KM;

    List<DeliveryRequest> capableDeliveries =
        deliveryRequestRepository.findByStatusAndWeightLessThanEqualAndDistanceLessThanEqual(
            DeliveryStatus.REQUESTED, vehicle.getMaxWeight(), vehicle.getMaxDistance());

    Location courierLocation = Location.of(currentLat, currentLon);

    return capableDeliveries.stream()
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
   * 특정 대기 주문을 배송원이 수락(Catch)한다. 동시 접속 환경에서 비관적 락({@code findByIdForUpdate})으로 단 1명만 수락 성공을 보장하며, 경합
   * 실패 시 AlreadyMatchedException(409 Conflict)을 던진다. 같은 차량이 동시에 서로 다른 배송 요청 두 건을 동시에 수락하는 것도, 차량
   * row 자체를 비관적 락으로 잠가서 막는다.
   */
  @Transactional
  public Matching catchDelivery(Long memberId, Long deliveryRequestId) {
    Member member = memberService.getById(memberId);

    if (member.getRole() != MemberRole.COURIER) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED, "배송원(COURIER)만 주문을 수락할 수 있습니다.");
    }

    // 여기서 Vehicle 엔티티를 통째로 불러오면(getActiveVehicle) 이 트랜잭션의 영속성 컨텍스트에 미리 캐시돼,
    // 뒤이은 getVehicleForUpdate가 실제로는 최신 값을 다시 읽어오지 못하고 이 캐시된(잠금 전) 인스턴스를 그대로 반환해버린다
    // (DB 행 잠금 자체는 걸리지만, 자바 객체의 필드값은 갱신되지 않는 Hibernate 1차 캐시 특성 때문). 그래서 id만 프로젝션으로 조회한다.
    Long vehicleId = vehicleService.getActiveVehicleId(memberId);

    try {
      DeliveryRequest deliveryRequest =
          deliveryRequestRepository
              .findByIdForUpdate(deliveryRequestId)
              .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));

      if (deliveryRequest.getStatus() != DeliveryStatus.REQUESTED) {
        throw new AlreadyMatchedException(deliveryRequestId, deliveryRequest.getStatus());
      }

      Vehicle vehicle = vehicleService.getVehicleForUpdate(vehicleId);
      if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
        throw new BusinessException(
            ErrorCode.VEHICLE_NOT_AVAILABLE, "영업 중(ONLINE) 상태에서만 주문을 수락할 수 있습니다.");
      }
      if (vehicle.getMaxWeight() < deliveryRequest.getWeight()
          || vehicle.getMaxDistance() < deliveryRequest.getDistance()) {
        throw new VehicleCapacityMismatchException(
            vehicle.getId(), deliveryRequest.getWeight(), deliveryRequest.getDistance());
      }

      deliveryRequest.setStatus(DeliveryStatus.MATCHED);
      deliveryRequestRepository.saveAndFlush(deliveryRequest);

      Matching matching = Matching.of(deliveryRequestId, vehicle.getId());
      Matching saved = matchingRepository.saveAndFlush(matching);

      vehicleService.markBusy(vehicle.getId());

      eventPublisher.publishEvent(
          new DeliveryStatusChangedEvent(
              deliveryRequestId, DeliveryStatus.MATCHED, LocalDateTime.now()));

      return saved;

    } catch (DataIntegrityViolationException e) {
      throw new AlreadyMatchedException(deliveryRequestId, DeliveryStatus.MATCHED);
    }
  }
}

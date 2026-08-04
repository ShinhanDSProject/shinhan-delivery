package com.example.shinhandelivery.tracking.service;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.delivery.service.MatchingService;
import com.example.shinhandelivery.tracking.dto.request.LocationUpdateRequest;
import com.example.shinhandelivery.tracking.dto.response.LocationBroadcastResponse;
import com.example.shinhandelivery.tracking.exception.UnauthorizedTrackingAccessException;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 배송원의 실시간 위치를 수신해 해당 배송을 구독 중인 클라이언트에게 브로드캐스트하는 유스케이스를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class TrackingService {

  private final MatchingService matchingService;
  private final VehicleService vehicleService;
  private final DeliveryService deliveryService;
  private final SimpMessagingTemplate messagingTemplate;

  /** 요청자가 해당 배송의 위치를 구독할 수 있는지 검증한다 — 배송 고객 본인이거나, 매칭된 차량의 소유주여야 한다. */
  @Transactional(readOnly = true)
  public void assertCanSubscribe(Long memberId, Long deliveryId) {
    DeliveryRequest delivery = deliveryService.getDeliveryRequest(deliveryId);
    if (delivery.getCustomerId().equals(memberId)) {
      return;
    }
    if (isMatchedVehicleOwner(memberId, deliveryId)) {
      return;
    }
    throw new UnauthorizedTrackingAccessException(deliveryId, memberId);
  }

  /**
   * 배송원이 보낸 위치를 검증 후 구독자에게 브로드캐스트한다. deliveryId는 발행 destination 경로에서 온 값으로, 요청 본문의 deliveryId와 일치해야
   * 하고 요청자는 매칭된 차량의 소유주여야 한다.
   */
  @Transactional(readOnly = true)
  public void broadcastLocation(Long memberId, Long deliveryId, LocationUpdateRequest request) {
    if (!deliveryId.equals(request.getDeliveryId())) {
      throw new UnauthorizedTrackingAccessException(deliveryId, memberId);
    }
    Matching matching = matchingService.getMatchingByDeliveryRequestId(deliveryId);
    if (!isOwner(memberId, matching.getVehicleId())) {
      throw new UnauthorizedTrackingAccessException(deliveryId, memberId);
    }

    LocationBroadcastResponse broadcast =
        new LocationBroadcastResponse(
            deliveryId,
            matching.getVehicleId(),
            request.getLatitude(),
            request.getLongitude(),
            request.getTimestamp());
    messagingTemplate.convertAndSend("/topic/delivery/" + deliveryId + "/location", broadcast);
  }

  /** 매칭 전(EntityNotFoundException)이면 아직 소유주가 없는 것으로 본다. */
  private boolean isMatchedVehicleOwner(Long memberId, Long deliveryId) {
    try {
      Matching matching = matchingService.getMatchingByDeliveryRequestId(deliveryId);
      return isOwner(memberId, matching.getVehicleId());
    } catch (EntityNotFoundException e) {
      return false;
    }
  }

  private boolean isOwner(Long memberId, Long vehicleId) {
    Vehicle vehicle = vehicleService.getById(vehicleId);
    return vehicle.getOwnerId().equals(memberId);
  }
}

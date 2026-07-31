package com.example.shinhangaecheokja.tracking.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.delivery.dto.response.MatchingResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.service.DeliveryService;
import com.example.shinhangaecheokja.delivery.service.MatchingService;
import com.example.shinhangaecheokja.tracking.dto.request.LocationUpdateRequest;
import com.example.shinhangaecheokja.tracking.dto.response.LocationBroadcastResponse;
import com.example.shinhangaecheokja.tracking.exception.UnauthorizedTrackingAccessException;
import com.example.shinhangaecheokja.vehicle.dto.response.VehicleResponse;
import com.example.shinhangaecheokja.vehicle.service.VehicleService;
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
  public void assertCanSubscribe(Long deliveryId, Long memberId) {
    DeliveryRequest delivery = deliveryService.getDeliveryRequest(deliveryId);
    if (delivery.getCustomerId().equals(memberId)) {
      return;
    }
    if (isMatchedVehicleOwner(deliveryId, memberId)) {
      return;
    }
    throw new UnauthorizedTrackingAccessException(deliveryId, memberId);
  }

  /**
   * 배송원이 보낸 위치를 검증 후 구독자에게 브로드캐스트한다. deliveryId는 발행 destination 경로에서 온 값으로, 요청 본문의 deliveryId와 일치해야
   * 하고 요청자는 매칭된 차량의 소유주여야 한다.
   */
  @Transactional(readOnly = true)
  public void broadcastLocation(Long deliveryId, LocationUpdateRequest request, Long memberId) {
    if (!deliveryId.equals(request.getDeliveryId())) {
      throw new UnauthorizedTrackingAccessException(deliveryId, memberId);
    }
    MatchingResponse matching = matchingService.getMatchingByDeliveryRequestId(deliveryId);
    if (!isOwner(matching.vehicleId(), memberId)) {
      throw new UnauthorizedTrackingAccessException(deliveryId, memberId);
    }

    LocationBroadcastResponse broadcast =
        new LocationBroadcastResponse(
            deliveryId,
            matching.vehicleId(),
            request.getLatitude(),
            request.getLongitude(),
            request.getTimestamp());
    messagingTemplate.convertAndSend("/topic/delivery/" + deliveryId + "/location", broadcast);
  }

  /** 매칭 전(EntityNotFoundException)이면 아직 소유주가 없는 것으로 본다. */
  private boolean isMatchedVehicleOwner(Long deliveryId, Long memberId) {
    try {
      MatchingResponse matching = matchingService.getMatchingByDeliveryRequestId(deliveryId);
      return isOwner(matching.vehicleId(), memberId);
    } catch (EntityNotFoundException e) {
      return false;
    }
  }

  private boolean isOwner(Long vehicleId, Long memberId) {
    VehicleResponse vehicle = vehicleService.getVehicle(vehicleId);
    return vehicle.ownerId().equals(memberId);
  }
}

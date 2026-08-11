package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryCancellationReason;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import java.time.LocalDateTime;

/** 배송 요청 상세 조회 응답 DTO. 목록용 {@link DeliveryListResponseDto}와 달리 배송원 이름·차량 종류·증거사진·타임라인 시각까지 포함한다. */
public record DeliveryDetailResponseDto(
    Long id,
    Long memberId,
    String pickupAddress,
    String dropoffAddress,
    double weight,
    double distance,
    DeliveryStatus status,
    long feePoint,
    double pickupLatitude,
    double pickupLongitude,
    double dropoffLatitude,
    double dropoffLongitude,
    ItemSize itemSize,
    String courierName,
    VehicleType vehicleType,
    String proofPhotoUrl,
    LocalDateTime createdAt,
    LocalDateTime matchedAt,
    LocalDateTime pickedUpAt,
    LocalDateTime completedAt,
    DeliveryCancellationReason cancellationReason,
    LocalDateTime cancelledAt,
    LocalDateTime refundedAt) {

  /**
   * DeliveryRequest 엔티티를 상세 응답 DTO로 변환한다. courierName·vehicleType·matchedAt은 아직 매칭된 배송원이 없으면 모두
   * null이다(호출자가 Matching·Vehicle을 미리 조회해 전달).
   */
  public static DeliveryDetailResponseDto from(
      DeliveryRequest entity,
      String courierName,
      LocalDateTime matchedAt,
      VehicleType vehicleType) {
    return new DeliveryDetailResponseDto(
        entity.getId(),
        entity.getMemberId(),
        entity.getPickupAddress(),
        entity.getDropoffAddress(),
        entity.getWeight(),
        entity.getDistance(),
        entity.getStatus(),
        entity.getFeePoint(),
        entity.getPickupLatitude(),
        entity.getPickupLongitude(),
        entity.getDropoffLatitude(),
        entity.getDropoffLongitude(),
        entity.getItemSize(),
        courierName,
        vehicleType,
        entity.getProofPhotoUrl(),
        entity.getCreatedAt(),
        matchedAt,
        entity.getPickedUpAt(),
        entity.getCompletedAt(),
        entity.getCancellationReason(),
        entity.getCancelledAt(),
        entity.getRefundedAt());
  }
}

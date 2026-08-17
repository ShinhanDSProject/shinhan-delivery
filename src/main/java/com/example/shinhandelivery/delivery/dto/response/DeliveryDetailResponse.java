package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryCancellationReason;
import com.example.shinhandelivery.delivery.entity.DeliveryInstructionType;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import java.time.LocalDateTime;
import lombok.Builder;

/** 배송 요청 상세 조회 응답 DTO. 목록용 {@link DeliveryListResponse}와 달리 배송원 이름·차량 종류·증거사진·타임라인 시각까지 포함한다. */
@Builder
public record DeliveryDetailResponse(
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
    String pickupPhotoUrl,
    DeliveryInstructionType deliveryInstructionType,
    String entranceCode,
    String unitDetail,
    String deliveryNote,
    String deliveryReferencePhotoUrl,
    LocalDateTime createdAt,
    LocalDateTime matchedAt,
    LocalDateTime pickedUpAt,
    LocalDateTime completedAt,
    DeliveryCancellationReason cancellationReason,
    LocalDateTime cancelledAt,
    LocalDateTime refundedAt,
    Long cancellationFee,
    Long refundAmount,
    Long courierCompensation,
    LocalDateTime compensatedAt) {

  /**
   * DeliveryRequest 엔티티를 상세 응답 DTO로 변환한다. courierName·vehicleType·matchedAt은 아직 매칭된 배송원이 없으면 모두
   * null이다(호출자가 Matching·Vehicle을 미리 조회해 전달).
   */
  public static DeliveryDetailResponse from(
      DeliveryRequest entity,
      String courierName,
      LocalDateTime matchedAt,
      VehicleType vehicleType) {
    return DeliveryDetailResponse.builder()
        .id(entity.getId())
        .memberId(entity.getMemberId())
        .pickupAddress(entity.getPickupAddress())
        .dropoffAddress(entity.getDropoffAddress())
        .weight(entity.getWeight())
        .distance(entity.getDistance())
        .status(entity.getStatus())
        .feePoint(entity.getFeePoint())
        .pickupLatitude(entity.getPickupLatitude())
        .pickupLongitude(entity.getPickupLongitude())
        .dropoffLatitude(entity.getDropoffLatitude())
        .dropoffLongitude(entity.getDropoffLongitude())
        .itemSize(entity.getItemSize())
        .courierName(courierName)
        .vehicleType(vehicleType)
        .proofPhotoUrl(entity.getProofPhotoUrl())
        .pickupPhotoUrl(entity.getPickupPhotoUrl())
        .deliveryInstructionType(entity.getDeliveryInstructionType())
        .entranceCode(entity.getEntranceCode())
        .unitDetail(entity.getUnitDetail())
        .deliveryNote(entity.getDeliveryNote())
        .deliveryReferencePhotoUrl(entity.getDeliveryReferencePhotoUrl())
        .createdAt(entity.getCreatedAt())
        .matchedAt(matchedAt)
        .pickedUpAt(entity.getPickedUpAt())
        .completedAt(entity.getCompletedAt())
        .cancellationReason(entity.getCancellationReason())
        .cancelledAt(entity.getCancelledAt())
        .refundedAt(entity.getRefundedAt())
        .cancellationFee(entity.getCancellationFee())
        .refundAmount(entity.getRefundAmount())
        .courierCompensation(entity.getCourierCompensation())
        .compensatedAt(entity.getCompensatedAt())
        .build();
  }
}

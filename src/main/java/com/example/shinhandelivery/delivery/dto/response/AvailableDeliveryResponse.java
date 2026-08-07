package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송원이 대기열(주변 신규 주문 목록)에서 조회를 위한 응답 DTO. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableDeliveryResponse {

  private Long deliveryRequestId;
  private String pickupAddress;
  private double pickupLatitude;
  private double pickupLongitude;
  private String dropoffAddress;
  private double dropoffLatitude;
  private double dropoffLongitude;
  private double weight;
  private double distanceKm;
  private double distanceToPickupKm;
  private long feePoint;
  private DeliveryStatus status;
  private ItemSize itemSize;
  private LocalDateTime createdAt;
}

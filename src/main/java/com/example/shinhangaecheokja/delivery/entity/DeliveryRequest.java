package com.example.shinhangaecheokja.delivery.entity;

import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요청 엔티티. customer_id는 Member를 가리키는 FK 값이다. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "delivery_request")
public class DeliveryRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Column(name = "pickup_address", nullable = false, length = 255)
  private String pickupAddress;

  @Column(name = "dropoff_address", nullable = false, length = 255)
  private String dropoffAddress;

  @Column(nullable = false)
  private double weight;

  @Column(nullable = false)
  private double distance;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DeliveryStatus status;

  @Column(name = "fee_point", nullable = false)
  private long feePoint;

  @Column(name = "pickup_latitude", nullable = false)
  private double pickupLatitude;

  @Column(name = "pickup_longitude", nullable = false)
  private double pickupLongitude;

  @Column(name = "dropoff_latitude", nullable = false)
  private double dropoffLatitude;

  @Column(name = "dropoff_longitude", nullable = false)
  private double dropoffLongitude;

  @Enumerated(EnumType.STRING)
  @Column(name = "item_size", nullable = false, length = 20)
  private ItemSize itemSize = ItemSize.MEDIUM;

  @Column(name = "proof_photo_url", length = 255)
  private String proofPhotoUrl;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "picked_up_at")
  private LocalDateTime pickedUpAt;

  /** DeliveryCreateRequest DTO 수용 기반으로 REQUESTED 상태의 DeliveryRequest 엔티티를 생성하는 정적 팩토리 메서드. */
  public static DeliveryRequest of(
      DeliveryCreateRequest request, double distanceKm, long feePoint) {
    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setCustomerId(request.getCustomerId());
    deliveryRequest.setPickupAddress(request.getPickupAddress());
    deliveryRequest.setDropoffAddress(request.getDropoffAddress());
    deliveryRequest.setWeight(request.getWeight());
    deliveryRequest.setDistance(distanceKm);
    deliveryRequest.setPickupLatitude(request.getPickupLatitude());
    deliveryRequest.setPickupLongitude(request.getPickupLongitude());
    deliveryRequest.setDropoffLatitude(request.getDropoffLatitude());
    deliveryRequest.setDropoffLongitude(request.getDropoffLongitude());
    deliveryRequest.setItemSize(request.getItemSize());
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    deliveryRequest.setFeePoint(feePoint);
    return deliveryRequest;
  }
}

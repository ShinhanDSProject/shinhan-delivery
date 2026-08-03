package com.example.shinhangaecheokja.delivery.entity;

import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryDistanceException;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryWeightException;
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

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /** DeliveryCreateRequest DTO 수용 기반으로 REQUESTED 상태의 DeliveryRequest 엔티티를 생성하는 정적 팩토리 메서드. */
  public static DeliveryRequest of(
      DeliveryCreateRequest request, double distanceKm, long feePoint) {
    if (request.getWeight() <= 0) {
      throw new InvalidDeliveryWeightException(request.getWeight());
    }
    if (distanceKm <= 0) {
      throw new InvalidDeliveryDistanceException(distanceKm);
    }

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
    deliveryRequest.setCreatedAt(LocalDateTime.now());
    return deliveryRequest;
  }

  /** DeliveryUpdateRequest DTO 기반으로 픽업지·도착지를 수정하는 도메인 비즈니스 메서드. */
  public DeliveryRequest updateBy(DeliveryUpdateRequest request) {
    this.pickupAddress = request.getPickupAddress();
    this.dropoffAddress = request.getDropoffAddress();
    return this;
  }

  /** 픽업 완료 처리를 수행하는 도메인 비즈니스 메서드. PICKED_UP 상태로 전이하고 픽업 시각을 기록한다. */
  public DeliveryRequest pickUp(LocalDateTime pickedUpAt) {
    this.status = DeliveryStatus.PICKED_UP;
    this.pickedUpAt = pickedUpAt;
    return this;
  }

  /** 배송 완료 처리를 수행하는 도메인 비즈니스 메서드. COMPLETED 상태로 전이하고 증거 사진과 완료 시각을 기록한다. */
  public DeliveryRequest complete(DeliveryCompleteRequest request, LocalDateTime completedAt) {
    this.status = DeliveryStatus.COMPLETED;
    this.proofPhotoUrl = request.getProofPhotoUrl();
    this.completedAt = completedAt;
    return this;
  }

  /** 배송 요청의 상태를 변경하는 도메인 비즈니스 메서드. */
  public DeliveryRequest changeStatus(DeliveryStatus newStatus) {
    this.status = newStatus;
    return this;
  }
}

package com.example.shinhandelivery.delivery.entity;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCompleteRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhandelivery.delivery.dto.request.DeliveryUpdateRequest;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryDistanceException;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryWeightException;
import com.example.shinhandelivery.member.entity.Member;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요청 엔티티. member_id는 Member를 가리키는 FK 값이다. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delivery_request")
public class DeliveryRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", insertable = false, updatable = false)
  private Member member;

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

  @Column(name = "payment_idempotency_key", length = 100)
  private String paymentIdempotencyKey;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(
        name = "latitude",
        column = @Column(name = "pickup_latitude", nullable = false)),
    @AttributeOverride(
        name = "longitude",
        column = @Column(name = "pickup_longitude", nullable = false))
  })
  @Builder.Default
  private Location pickupLocation = Location.of(0.0, 0.0);

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(
        name = "latitude",
        column = @Column(name = "dropoff_latitude", nullable = false)),
    @AttributeOverride(
        name = "longitude",
        column = @Column(name = "dropoff_longitude", nullable = false))
  })
  @Builder.Default
  private Location dropoffLocation = Location.of(0.0, 0.0);

  @Enumerated(EnumType.STRING)
  @Column(name = "item_size", nullable = false, length = 20)
  @Builder.Default
  private ItemSize itemSize = ItemSize.MEDIUM;

  @Column(name = "proof_photo_url", length = 255)
  private String proofPhotoUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "delivery_instruction_type", nullable = false, length = 40)
  @Builder.Default
  private DeliveryInstructionType deliveryInstructionType = DeliveryInstructionType.NONE;

  @Column(name = "entrance_code", length = 100)
  private String entranceCode;

  @Column(name = "unit_detail", length = 100)
  private String unitDetail;

  @Column(name = "delivery_note", length = 500)
  private String deliveryNote;

  @Column(name = "delivery_reference_photo_url", length = 255)
  private String deliveryReferencePhotoUrl;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "picked_up_at")
  private LocalDateTime pickedUpAt;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "cancellation_reason", length = 30)
  private DeliveryCancellationReason cancellationReason;

  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;

  @Column(name = "refunded_at")
  private LocalDateTime refundedAt;

  public double getPickupLatitude() {
    return pickupLocation != null ? pickupLocation.getLatitude() : 0.0;
  }

  public double getPickupLongitude() {
    return pickupLocation != null ? pickupLocation.getLongitude() : 0.0;
  }

  public double getDropoffLatitude() {
    return dropoffLocation != null ? dropoffLocation.getLatitude() : 0.0;
  }

  public double getDropoffLongitude() {
    return dropoffLocation != null ? dropoffLocation.getLongitude() : 0.0;
  }

  public void setPickupLatitude(double latitude) {
    double currentLon = pickupLocation != null ? pickupLocation.getLongitude() : 0.0;
    this.pickupLocation = Location.of(latitude, currentLon);
  }

  public void setPickupLongitude(double longitude) {
    double currentLat = pickupLocation != null ? pickupLocation.getLatitude() : 0.0;
    this.pickupLocation = Location.of(currentLat, longitude);
  }

  public void setDropoffLatitude(double latitude) {
    double currentLon = dropoffLocation != null ? dropoffLocation.getLongitude() : 0.0;
    this.dropoffLocation = Location.of(latitude, currentLon);
  }

  public void setDropoffLongitude(double longitude) {
    double currentLat = dropoffLocation != null ? dropoffLocation.getLatitude() : 0.0;
    this.dropoffLocation = Location.of(currentLat, longitude);
  }

  /** DeliveryCreateRequest DTO 수용 기반으로 REQUESTED 상태의 DeliveryRequest 엔티티를 생성하는 정적 팩토리 메서드. */
  public static DeliveryRequest of(
      Long memberId, DeliveryCreateRequest request, double distanceKm, long feePoint) {
    if (request.getWeight() <= 0) {
      throw new InvalidDeliveryWeightException(request.getWeight());
    }
    if (distanceKm <= 0) {
      throw new InvalidDeliveryDistanceException(distanceKm);
    }
    validateDeliveryInstructions(request);

    return DeliveryRequest.builder()
        .memberId(memberId)
        .pickupAddress(request.getPickupAddress())
        .dropoffAddress(request.getDropoffAddress())
        .weight(request.getWeight())
        .distance(distanceKm)
        .pickupLocation(request.getPickupLocation())
        .dropoffLocation(request.getDropoffLocation())
        .itemSize(request.getItemSize())
        .deliveryInstructionType(resolveInstructionType(request.getDeliveryInstructionType()))
        .entranceCode(normalize(request.getEntranceCode()))
        .unitDetail(normalize(request.getUnitDetail()))
        .deliveryNote(normalize(request.getDeliveryNote()))
        .deliveryReferencePhotoUrl(normalize(request.getDeliveryReferencePhotoUrl()))
        .status(DeliveryStatus.REQUESTED)
        .feePoint(feePoint)
        .createdAt(LocalDateTime.now())
        .build();
  }

  private static void validateDeliveryInstructions(DeliveryCreateRequest request) {
    DeliveryInstructionType type = resolveInstructionType(request.getDeliveryInstructionType());
    if (type == DeliveryInstructionType.ENTRANCE_CODE
        && normalize(request.getEntranceCode()) == null) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
    if (type == DeliveryInstructionType.CUSTOM && normalize(request.getDeliveryNote()) == null) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
  }

  private static DeliveryInstructionType resolveInstructionType(DeliveryInstructionType type) {
    return type == null ? DeliveryInstructionType.NONE : type;
  }

  private static String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
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

  /** 30분 미배정 타임아웃으로 요청을 취소하고, 결제 요청이면 환불 완료 시각을 함께 기록한다. */
  public DeliveryRequest cancelByTimeout(LocalDateTime processedAt, boolean refunded) {
    this.status = DeliveryStatus.CANCELLED;
    this.cancellationReason = DeliveryCancellationReason.AUTO_TIMEOUT;
    this.cancelledAt = processedAt;
    this.refundedAt = refunded ? processedAt : null;
    return this;
  }
}

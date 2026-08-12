package com.example.shinhandelivery.vehicle.entity;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhandelivery.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhandelivery.vehicle.exception.InvalidWeightException;
import com.example.shinhandelivery.vehicle.exception.OverMaxDistanceException;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 운송수단(드론/오토바이/차량) 엔티티. member_id는 Member를 가리키는 FK 값이다. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehicle")
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", insertable = false, updatable = false)
  private Member member;

  @Column(length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private VehicleType type;

  @Column(name = "max_weight", nullable = false)
  private double maxWeight;

  @Column(name = "max_distance", nullable = false)
  private double maxDistance;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "latitude", column = @Column(name = "latitude", nullable = false)),
    @AttributeOverride(name = "longitude", column = @Column(name = "longitude", nullable = false))
  })
  @Builder.Default
  private Location location = Location.of(0.0, 0.0);

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private VehicleStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "approval_status", nullable = false, length = 20)
  @Builder.Default
  private VehicleApprovalStatus approvalStatus = VehicleApprovalStatus.APPROVED;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private boolean isActive = false;

  @Column(name = "license_plate_number", length = 50)
  private String licensePlateNumber;

  @Column(name = "insurance_photo_url", columnDefinition = "LONGTEXT")
  private String insurancePhotoUrl;

  @Column(name = "photo_url", columnDefinition = "LONGTEXT")
  private String photoUrl;

  @Column(name = "displacement")
  private Integer displacement;

  public double getLatitude() {
    return location != null ? location.getLatitude() : 0.0;
  }

  public double getLongitude() {
    return location != null ? location.getLongitude() : 0.0;
  }

  public void setLatitude(double latitude) {
    double currentLon = location != null ? location.getLongitude() : 0.0;
    this.location = Location.of(latitude, currentLon);
  }

  public void setLongitude(double longitude) {
    double currentLat = location != null ? location.getLatitude() : 0.0;
    this.location = Location.of(currentLat, longitude);
  }

  /** 회원 가입 시 기본 생성되는 AVAILABLE 및 APPROVED 상태의 Vehicle 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Vehicle createDefault(
      Long memberId, VehicleType type, double maxWeight, double maxDistance) {
    return createDefault(memberId, null, type, maxWeight, maxDistance);
  }

  /** 회원 가입 시 지정된 장비 이름을 가진 AVAILABLE 및 APPROVED 상태의 Vehicle 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Vehicle createDefault(
      Long memberId, String name, VehicleType type, double maxWeight, double maxDistance) {
    String defaultName = type != null ? type.name() : "기본 장비";
    String finalName = (name != null && !name.isBlank()) ? name : defaultName;

    return Vehicle.builder()
        .memberId(memberId)
        .name(finalName)
        .type(type)
        .maxWeight(maxWeight)
        .maxDistance(maxDistance)
        .location(Location.of(0.0, 0.0))
        .status(VehicleStatus.AVAILABLE)
        .approvalStatus(VehicleApprovalStatus.APPROVED)
        .isActive(true)
        .build();
  }

  /** VehicleCreateRequest DTO 기반으로 Vehicle 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Vehicle from(VehicleCreateRequest request) {
    VehicleApprovalStatus defaultApproval =
        (request.getType() == VehicleType.MOTORCYCLE
                || request.getType() == VehicleType.CAR
                || request.getType() == VehicleType.DRONE)
            ? VehicleApprovalStatus.PENDING
            : VehicleApprovalStatus.APPROVED;

    Vehicle vehicle =
        Vehicle.builder()
            .memberId(request.getMemberId())
            .name(request.getName() != null && !request.getName().isBlank() ? request.getName() : (request.getType() != null ? request.getType().name() : "신규 장비"))
            .type(request.getType())
            .maxWeight(request.getMaxWeight())
            .maxDistance(request.getMaxDistance())
            .displacement(request.getDisplacement())
            .licensePlateNumber(request.getLicensePlateNumber())
            .insurancePhotoUrl(request.getInsurancePhotoUrl())
            .photoUrl(request.getPhotoUrl())
            .location(request.getLocation() != null ? request.getLocation() : Location.of(0.0, 0.0))
            .status(VehicleStatus.AVAILABLE)
            .approvalStatus(defaultApproval)
            .isActive(false)
            .build();
    vehicle.validateInvariants();
    return vehicle;
  }

  /** VehicleUpdateRequest DTO 기반으로 Vehicle 정보를 수정하는 도메인 비즈니스 메서드. */
  public Vehicle updateBy(VehicleUpdateRequest request) {
    if (request.getName() != null && !request.getName().isBlank()) {
      this.name = request.getName();
    }
    this.type = request.getType();
    this.maxWeight = request.getMaxWeight();
    if (request.getMaxDistance() > 0) {
      this.maxDistance = request.getMaxDistance();
    }
    if (request.getDisplacement() != null) {
      this.displacement = request.getDisplacement();
    }
    if (request.getLicensePlateNumber() != null) {
      this.licensePlateNumber = request.getLicensePlateNumber();
    }
    if (request.getInsurancePhotoUrl() != null) {
      this.insurancePhotoUrl = request.getInsurancePhotoUrl();
    }
    if (request.getPhotoUrl() != null) {
      this.photoUrl = request.getPhotoUrl();
    }
    if (request.getLocation() != null) {
      this.location = request.getLocation();
    }
    validateInvariants();
    return this;
  }

  /** 이 차량 스펙(최대 적재 중량·운행 거리)이 도메인 불변성을 만족하는지 스스로 검증한다. */
  private void validateInvariants() {
    if (maxWeight <= 0) {
      throw new InvalidWeightException(maxWeight);
    }
    if (maxDistance <= 0) {
      throw new OverMaxDistanceException(maxDistance);
    }
  }

  /** Vehicle의 운영 상태를 변경하는 도메인 비즈니스 메서드. */
  public Vehicle markAs(VehicleStatus newStatus) {
    this.status = newStatus;
    return this;
  }

  /** 장비를 활성화(ON)한다. */
  public void activate() {
    this.isActive = true;
  }

  /** 장비를 비활성화(OFF)한다. */
  public void deactivate() {
    this.isActive = false;
  }

  /** 관리자가 장비를 승인한다. */
  public void approve() {
    this.approvalStatus = VehicleApprovalStatus.APPROVED;
  }

  /** 관리자가 장비를 반려한다. */
  public void reject() {
    this.approvalStatus = VehicleApprovalStatus.REJECTED;
    this.isActive = false;
  }
}

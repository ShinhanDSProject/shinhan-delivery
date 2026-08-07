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

  /** 회원 가입 시 기본 생성되는 AVAILABLE 상태의 Vehicle 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Vehicle createDefault(
      Long memberId, VehicleType type, double maxWeight, double maxDistance) {
    return Vehicle.builder()
        .memberId(memberId)
        .type(type)
        .maxWeight(maxWeight)
        .maxDistance(maxDistance)
        .location(Location.of(0.0, 0.0))
        .status(VehicleStatus.AVAILABLE)
        .build();
  }

  /** VehicleCreateRequest DTO 기반으로 AVAILABLE 상태의 Vehicle 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Vehicle from(VehicleCreateRequest request) {
    Vehicle vehicle =
        Vehicle.builder()
            .memberId(request.getMemberId())
            .type(request.getType())
            .maxWeight(request.getMaxWeight())
            .maxDistance(request.getMaxDistance())
            .location(request.getLocation())
            .status(VehicleStatus.AVAILABLE)
            .build();
    vehicle.validateInvariants();
    return vehicle;
  }

  /** VehicleUpdateRequest DTO 기반으로 Vehicle 정보를 수정하는 도메인 비즈니스 메서드. */
  public Vehicle updateBy(VehicleUpdateRequest request) {
    this.type = request.getType();
    this.maxWeight = request.getMaxWeight();
    this.maxDistance = request.getMaxDistance();
    this.location = request.getLocation();
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
}

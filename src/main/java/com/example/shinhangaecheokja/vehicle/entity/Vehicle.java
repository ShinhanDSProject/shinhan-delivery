package com.example.shinhangaecheokja.vehicle.entity;

import com.example.shinhangaecheokja.vehicle.dto.request.VehicleCreateRequest;
import com.example.shinhangaecheokja.vehicle.dto.request.VehicleUpdateRequest;
import com.example.shinhangaecheokja.vehicle.exception.InvalidWeightException;
import com.example.shinhangaecheokja.vehicle.exception.OverMaxDistanceException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 운송수단(드론/오토바이/차량) 엔티티. owner_id는 Member를 가리키는 FK 값이다. */
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

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private VehicleType type;

  @Column(name = "max_weight", nullable = false)
  private double maxWeight;

  @Column(name = "max_distance", nullable = false)
  private double maxDistance;

  @Column(nullable = false)
  private double latitude;

  @Column(nullable = false)
  private double longitude;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private VehicleStatus status;

  /** VehicleCreateRequest DTO 기반으로 AVAILABLE 상태의 Vehicle 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Vehicle from(VehicleCreateRequest request) {
    validateInvariants(request.getMaxWeight(), request.getMaxDistance());
    return Vehicle.builder()
        .ownerId(request.getOwnerId())
        .type(request.getType())
        .maxWeight(request.getMaxWeight())
        .maxDistance(request.getMaxDistance())
        .latitude(request.getLatitude())
        .longitude(request.getLongitude())
        .status(VehicleStatus.AVAILABLE)
        .build();
  }

  /** VehicleUpdateRequest DTO 기반으로 Vehicle 정보를 수정하는 도메인 비즈니스 메서드. */
  public Vehicle updateBy(VehicleUpdateRequest request) {
    validateInvariants(request.getMaxWeight(), request.getMaxDistance());
    this.type = request.getType();
    this.maxWeight = request.getMaxWeight();
    this.maxDistance = request.getMaxDistance();
    this.latitude = request.getLatitude();
    this.longitude = request.getLongitude();
    return this;
  }

  private static void validateInvariants(double maxWeight, double maxDistance) {
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

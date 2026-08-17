package com.example.shinhandelivery.vehicle.dto.response;

import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleApprovalStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import lombok.Builder;

/** 운송수단 응답 DTO. */
@Builder
public record VehicleResponse(
    Long id,
    Long memberId,
    String name,
    VehicleType type,
    double maxWeight,
    double maxDistance,
    Integer displacement,
    String licensePlateNumber,
    String insurancePhotoUrl,
    String photoUrl,
    double latitude,
    double longitude,
    VehicleStatus status,
    VehicleApprovalStatus approvalStatus,
    boolean isActive) {

  /** Vehicle 엔티티를 응답 DTO로 변환한다. */
  public static VehicleResponse from(Vehicle entity) {
    return VehicleResponse.builder()
        .id(entity.getId())
        .memberId(entity.getMemberId())
        .name(entity.getName())
        .type(entity.getType())
        .maxWeight(entity.getMaxWeight())
        .maxDistance(entity.getMaxDistance())
        .displacement(entity.getDisplacement())
        .licensePlateNumber(entity.getLicensePlateNumber())
        .insurancePhotoUrl(entity.getInsurancePhotoUrl())
        .photoUrl(entity.getPhotoUrl())
        .latitude(entity.getLatitude())
        .longitude(entity.getLongitude())
        .status(entity.getStatus())
        .approvalStatus(entity.getApprovalStatus())
        .isActive(entity.isActive())
        .build();
  }
}

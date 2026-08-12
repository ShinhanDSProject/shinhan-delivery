package com.example.shinhandelivery.vehicle.dto.response;

import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleApprovalStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;

/** 운송수단 응답 DTO. */
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
    return new VehicleResponse(
        entity.getId(),
        entity.getMemberId(),
        entity.getName(),
        entity.getType(),
        entity.getMaxWeight(),
        entity.getMaxDistance(),
        entity.getDisplacement(),
        entity.getLicensePlateNumber(),
        entity.getInsurancePhotoUrl(),
        entity.getPhotoUrl(),
        entity.getLatitude(),
        entity.getLongitude(),
        entity.getStatus(),
        entity.getApprovalStatus(),
        entity.isActive());
  }
}

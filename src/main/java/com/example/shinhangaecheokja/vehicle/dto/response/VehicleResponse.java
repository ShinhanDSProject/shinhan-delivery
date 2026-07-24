package com.example.shinhangaecheokja.dto.response;

import com.example.shinhangaecheokja.entity.Vehicle;
import com.example.shinhangaecheokja.entity.VehicleType;

/** 운송수단 응답 DTO. */
public record VehicleResponse(
    Long id, Long ownerId, VehicleType type, double maxWeight, double maxDistance) {

  /** Vehicle 엔티티를 응답 DTO로 변환한다. */
  public static VehicleResponse from(Vehicle entity) {
    return new VehicleResponse(
        entity.getId(),
        entity.getOwnerId(),
        entity.getType(),
        entity.getMaxWeight(),
        entity.getMaxDistance());
  }
}

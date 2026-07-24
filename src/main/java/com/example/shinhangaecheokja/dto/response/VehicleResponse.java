package com.example.shinhangaecheokja.dto.response;

import com.example.shinhangaecheokja.entity.Vehicle;
import com.example.shinhangaecheokja.entity.VehicleType;

public record VehicleResponse(
    Long id, Long ownerId, VehicleType type, double maxWeight, double maxDistance) {

  public static VehicleResponse from(Vehicle entity) {
    return new VehicleResponse(
        entity.getId(),
        entity.getOwnerId(),
        entity.getType(),
        entity.getMaxWeight(),
        entity.getMaxDistance());
  }
}

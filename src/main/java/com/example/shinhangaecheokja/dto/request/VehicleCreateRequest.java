package com.example.shinhangaecheokja.dto.request;

import com.example.shinhangaecheokja.entity.VehicleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleCreateRequest {

  private Long ownerId;
  private VehicleType type;
  private double maxWeight;
  private double maxDistance;
}

package com.example.shinhangaecheokja.dto.request;

import com.example.shinhangaecheokja.entity.VehicleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 운송수단 수정 요청 DTO. ownerId는 변경 대상이 아니다. */
@Getter
@Setter
@NoArgsConstructor
public class VehicleUpdateRequest {

  private VehicleType type;
  private double maxWeight;
  private double maxDistance;
}

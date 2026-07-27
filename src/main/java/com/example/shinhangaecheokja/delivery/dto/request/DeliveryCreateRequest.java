package com.example.shinhangaecheokja.delivery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송 요청 생성 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class DeliveryCreateRequest {

  private Long customerId;
  private String pickupAddress;
  private String dropoffAddress;
  private double weight;
  private double distance;
  private double pickupLatitude;
  private double pickupLongitude;
}

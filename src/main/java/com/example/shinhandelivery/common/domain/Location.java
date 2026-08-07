package com.example.shinhandelivery.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/** 위도(latitude)와 경도(longitude)를 하나로 묶어 표현하는 불변 위치 값 객체 (Value Object / Embeddable). */
@Embeddable
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

  private static final double EARTH_RADIUS_KM = 6371.0;
  private static final double MIN_LATITUDE = -90.0;
  private static final double MAX_LATITUDE = 90.0;
  private static final double MIN_LONGITUDE = -180.0;
  private static final double MAX_LONGITUDE = 180.0;

  @Column(name = "latitude")
  private double latitude;

  @Column(name = "longitude")
  private double longitude;

  public Location(double latitude, double longitude) {
    validateCoordinates(latitude, longitude);
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public static Location of(double latitude, double longitude) {
    return new Location(latitude, longitude);
  }

  /**
   * 다른 Location과의 대권 거리를 하버사인(Haversine) 공식으로 계산한다 (단위: km).
   *
   * @param target 목적지 위치
   * @return 직선 거리(km)
   */
  public double distanceToKm(Location target) {
    if (target == null) {
      throw new IllegalArgumentException("목적지 위치(target)는 null일 수 없습니다.");
    }
    double dLat = Math.toRadians(target.latitude - this.latitude);
    double dLon = Math.toRadians(target.longitude - this.longitude);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(this.latitude))
                * Math.cos(Math.toRadians(target.latitude))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }

  private static void validateCoordinates(double latitude, double longitude) {
    if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
      throw new IllegalArgumentException(
          String.format(
              "위도(latitude)는 %.1f~%.1f 범위 내여야 합니다. (입력값: %.6f)",
              MIN_LATITUDE, MAX_LATITUDE, latitude));
    }
    if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
      throw new IllegalArgumentException(
          String.format(
              "경도(longitude)는 %.1f~%.1f 범위 내여야 합니다. (입력값: %.6f)",
              MIN_LONGITUDE, MAX_LONGITUDE, longitude));
    }
  }
}

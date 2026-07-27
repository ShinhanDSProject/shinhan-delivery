package com.example.shinhangaecheokja.delivery.util;

/** 두 좌표(위도/경도) 사이의 거리를 하버사인 공식으로 계산한다. */
public final class HaversineDistanceCalculator {

  private static final double EARTH_RADIUS_KM = 6371.0;

  private HaversineDistanceCalculator() {}

  /** 두 좌표 사이의 거리를 km 단위로 반환한다. */
  public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    a = Math.min(1.0, Math.max(0.0, a));
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }
}

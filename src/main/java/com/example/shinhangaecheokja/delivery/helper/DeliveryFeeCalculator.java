package com.example.shinhangaecheokja.delivery.helper;

import com.example.shinhangaecheokja.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhangaecheokja.delivery.entity.ItemSize;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/**
 * 배송 거리·요금 산정 공식을 전담하는 계산기. DeliveryService의 requestDelivery와 estimateFee가 동일한 공식을 공유하기 위해 이 클래스를
 * 거친다.
 */
@Component
public class DeliveryFeeCalculator {

  private static final double EARTH_RADIUS_KM = 6371.0;
  private static final BigDecimal ESTIMATE_BASE_FEE = BigDecimal.valueOf(3000);
  private static final BigDecimal ESTIMATE_FEE_PER_KM = BigDecimal.valueOf(500);
  private static final BigDecimal ESTIMATE_FEE_PER_KG = BigDecimal.valueOf(200);
  private static final BigDecimal SIZE_SURCHARGE_RATE_MEDIUM = BigDecimal.valueOf(0.30);
  private static final BigDecimal SIZE_SURCHARGE_RATE_LARGE = BigDecimal.valueOf(0.60);

  /** 두 좌표(위도/경도) 간의 대권 거리를 하버사인 공식으로 계산한다(단위: km). */
  public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }

  /**
   * 기본료 + 거리 할증(하버사인 거리 × km당 요금) + 무게 할증(무게 × kg당 요금)의 소계에, 물품 크기별 할증률(SMALL 0%/MEDIUM 30%/LARGE
   * 60%)을 곱한 크기 할증을 더한다.
   */
  public DeliveryEstimateResponse calculateFee(
      double distanceKm, double weight, ItemSize itemSize) {
    BigDecimal distanceSurcharge =
        ESTIMATE_FEE_PER_KM
            .multiply(BigDecimal.valueOf(distanceKm))
            .setScale(0, RoundingMode.HALF_UP);
    BigDecimal weightSurcharge =
        ESTIMATE_FEE_PER_KG.multiply(BigDecimal.valueOf(weight)).setScale(0, RoundingMode.HALF_UP);
    BigDecimal subtotal = ESTIMATE_BASE_FEE.add(distanceSurcharge).add(weightSurcharge);
    BigDecimal sizeSurcharge =
        subtotal.multiply(sizeSurchargeRate(itemSize)).setScale(0, RoundingMode.HALF_UP);
    BigDecimal totalFee = subtotal.add(sizeSurcharge);

    return new DeliveryEstimateResponse(
        ESTIMATE_BASE_FEE, distanceSurcharge, weightSurcharge, sizeSurcharge, totalFee);
  }

  private BigDecimal sizeSurchargeRate(ItemSize itemSize) {
    return switch (itemSize) {
      case SMALL -> BigDecimal.ZERO;
      case MEDIUM -> SIZE_SURCHARGE_RATE_MEDIUM;
      case LARGE -> SIZE_SURCHARGE_RATE_LARGE;
    };
  }
}

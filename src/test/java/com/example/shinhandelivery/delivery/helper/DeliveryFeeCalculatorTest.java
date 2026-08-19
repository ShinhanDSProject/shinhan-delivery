package com.example.shinhandelivery.delivery.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

import com.example.shinhandelivery.common.domain.Location;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.dto.response.DeliveryEstimateResponse;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeliveryFeeCalculatorTest {

  private DeliveryFeeCalculator calculator;

  @BeforeEach
  void setUp() {
    calculator = new DeliveryFeeCalculator();
  }

  @Test
  @DisplayName("Location 객체를 이용하여 두 지점 간의 거리를 정확히 계산한다.")
  void calculateDistanceKmWithLocation() {
    // given
    Location gangnam = Location.of(37.4979, 127.0276);
    Location yeoksam = Location.of(37.5006, 127.0364);

    // when
    double distanceKm = calculator.calculateDistanceKm(gangnam, yeoksam);

    // then
    assertThat(distanceKm).isCloseTo(0.83, offset(0.05));
  }

  @Test
  @DisplayName("Location 인자가 null인 경우 예외가 발생한다.")
  void calculateDistanceKmNullLocation() {
    Location loc = Location.of(37.5, 127.0);

    assertThatThrownBy(() -> calculator.calculateDistanceKm(null, loc))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_LOCATION_TARGET);

    assertThatThrownBy(() -> calculator.calculateDistanceKm(loc, null))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_LOCATION_TARGET);
  }

  @Test
  @DisplayName("거리, 무게, 물품 크기를 기반으로 예상 배송 요금을 정확히 계산한다.")
  void calculateFee() {
    // given: 거리 2km, 무게 3kg, MEDIUM 크기(할증 30%)
    double distanceKm = 2.0;
    double weight = 3.0;
    ItemSize itemSize = ItemSize.MEDIUM;

    // when
    DeliveryEstimateResponse response = calculator.calculateFee(distanceKm, weight, itemSize);

    // then
    // 기본료: 3000
    // 거리할증: 2 * 500 = 1000
    // 무게할증: 3 * 200 = 600
    // 소계: 3000 + 1000 + 600 = 4600
    // 크기할증(30%): 4600 * 0.3 = 1380
    // 최종요금: 4600 + 1380 = 5980
    assertThat(response.baseFee()).isEqualTo(BigDecimal.valueOf(3000));
    assertThat(response.distanceSurcharge()).isEqualTo(BigDecimal.valueOf(1000));
    assertThat(response.weightSurcharge()).isEqualTo(BigDecimal.valueOf(600));
    assertThat(response.sizeSurcharge()).isEqualTo(BigDecimal.valueOf(1380));
    assertThat(response.totalFee()).isEqualTo(BigDecimal.valueOf(5980));
  }
}

package com.example.shinhangaecheokja.delivery.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class HaversineDistanceCalculatorTest {

  @Test
  void 동일한_좌표는_거리가_0이다() {
    double distance = HaversineDistanceCalculator.distanceKm(37.5665, 126.9780, 37.5665, 126.9780);

    assertThat(distance).isEqualTo(0.0);
  }

  @Test
  void 서울과_부산의_거리를_근사값으로_계산한다() {
    double distance =
        HaversineDistanceCalculator.distanceKm(37.5665, 126.9780, 35.1796, 129.0756);

    assertThat(distance).isCloseTo(325.0, within(10.0));
  }
}

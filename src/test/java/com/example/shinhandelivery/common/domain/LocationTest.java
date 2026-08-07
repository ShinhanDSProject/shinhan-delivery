package com.example.shinhandelivery.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LocationTest {

  @Test
  @DisplayName("유효한 위도와 경도로 Location 객체를 생성할 수 있다.")
  void createLocationSuccess() {
    // given
    double latitude = 37.5665;
    double longitude = 126.9780;

    // when
    Location location = Location.of(latitude, longitude);

    // then
    assertThat(location.getLatitude()).isEqualTo(latitude);
    assertThat(location.getLongitude()).isEqualTo(longitude);
  }

  @Test
  @DisplayName("위도가 -90~90 범위를 벗어나면 예외가 발생한다.")
  void createLocationInvalidLatitude() {
    assertThatThrownBy(() -> Location.of(91.0, 127.0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("위도(latitude)는 -90.0~90.0 범위 내여야 합니다.");

    assertThatThrownBy(() -> Location.of(-90.1, 127.0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("위도(latitude)는 -90.0~90.0 범위 내여야 합니다.");
  }

  @Test
  @DisplayName("경도가 -180~180 범위를 벗어나면 예외가 발생한다.")
  void createLocationInvalidLongitude() {
    assertThatThrownBy(() -> Location.of(37.5, 181.0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("경도(longitude)는 -180.0~180.0 범위 내여야 합니다.");

    assertThatThrownBy(() -> Location.of(37.5, -180.1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("경도(longitude)는 -180.0~180.0 범위 내여야 합니다.");
  }

  @Test
  @DisplayName("두 위치(Location) 간의 거리를 정확히 계산한다.")
  void calculateDistanceToKm() {
    // given: 강남역 (37.4979, 127.0276), 역삼역 (37.5006, 127.0364)
    Location gangnam = Location.of(37.4979, 127.0276);
    Location yeoksam = Location.of(37.5006, 127.0364);

    // when
    double distanceKm = gangnam.distanceToKm(yeoksam);

    // then: 약 0.83km (허용 오차 0.05km 이내)
    assertThat(distanceKm).isCloseTo(0.83, offset(0.05));
  }

  @Test
  @DisplayName("같은 위도와 경도를 가진 Location은 동등하다 (Equals & HashCode).")
  void equalsAndHashCode() {
    Location loc1 = Location.of(37.5, 127.0);
    Location loc2 = Location.of(37.5, 127.0);

    assertThat(loc1).isEqualTo(loc2);
    assertThat(loc1.hashCode()).isEqualTo(loc2.hashCode());
  }
}

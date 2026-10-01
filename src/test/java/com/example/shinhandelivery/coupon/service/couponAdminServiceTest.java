package com.example.shinhandelivery.coupon.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.coupon.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponAdminServiceTest {

  @InjectMocks private CouponAdminService couponAdminService;

  // CouponAdminService의 생성자 인자/필드에 맞춰 @Mock을 추가해 주세요!
  // 예: CouponAdminService(CouponRepository couponRepository) 인 경우
  @Mock private CouponRepository couponRepository;

  @Test
  @DisplayName("서비스 초기화 테스트")
  void testInit() {
    assertThat(couponAdminService).isNotNull();
  }
}

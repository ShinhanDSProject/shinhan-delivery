package com.example.shinhandelivery.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.example.shinhandelivery.member.dto.request.MemberCreateRequest;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTransactionTest {

  @Autowired private MemberService memberService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private PaymentRepository paymentRepository;

  @MockitoBean private VehicleRepository vehicleRepository;

  @Test
  @DisplayName("배송원 가입 중 차량 저장이 실패하면 회원 생성도 롤백된다")
  void createCourierRollsBackWhenVehicleSaveFails() {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("rollback-courier@example.com");
    request.setPassword("Password123!");
    request.setName("롤백배송");
    request.setPhoneNumber("010-2222-3333");
    request.setRole(MemberRole.COURIER);
    request.setVehicleType(VehicleType.CAR);
    request.setActivityRegion("서울");
    request.setPreferredWeight(20.0);

    doThrow(new RuntimeException("vehicle save failed")).when(vehicleRepository).save(any());

    assertThatThrownBy(() -> memberService.create(request)).isInstanceOf(RuntimeException.class);
    assertThat(memberRepository.findByEmail("rollback-courier@example.com")).isEmpty();
    assertThat(paymentRepository.findByMemberId(1L)).isEmpty();
  }
}

package com.example.shinhangaecheokja.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.repository.VehicleRepository;
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

  @MockitoBean private VehicleRepository vehicleRepository;

  @Test
  void courierSignupShouldRollbackMemberWhenVehiclePersistenceFails() {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("rollback-courier@example.com");
    request.setPassword("Password123!");
    request.setName("롤백 배송원");
    request.setPhoneNumber("010-2222-3333");
    request.setRole(MemberRole.COURIER);
    request.setVehicleType(VehicleType.CAR);
    request.setActivityRegion("서울특별시 송파구");
    request.setPreferredWeight(20.0);

    when(vehicleRepository.save(any(Vehicle.class)))
        .thenThrow(new RuntimeException("vehicle persistence failed"));

    assertThatThrownBy(() -> memberService.create(request))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("vehicle persistence failed");

    assertThat(memberRepository.findByEmail("rollback-courier@example.com")).isEmpty();
  }
}

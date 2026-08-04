package com.example.shinhangaecheokja.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.request.MemberProfileUpdateRequestDto;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.exception.DuplicateMemberException;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.repository.VehicleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

  @Mock private MemberRepository memberRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private VehicleRepository vehicleRepository;
  @InjectMocks private MemberService memberService;

  @Test
  @DisplayName("이메일이 중복되지 않으면 회원을 생성한다")
  void createMemberSuccess() {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("user@example.com");
    request.setPassword("password123");
    request.setName("홍길동");
    request.setPhoneNumber("010-1234-5678");
    request.setRole(MemberRole.CUSTOMER);

    when(memberRepository.existsByEmail("user@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
    when(memberRepository.save(any(Member.class)))
        .thenAnswer(
            invocation -> {
              Member member = invocation.getArgument(0);
              member.setId(1L);
              return member;
            });

    Member response = memberService.create(request);

    assertThat(response.getEmail()).isEqualTo("user@example.com");
    assertThat(response.getRole()).isEqualTo(MemberRole.CUSTOMER);
  }

  @Test
  @DisplayName("배송원 회원가입이면 기본 차량도 함께 생성한다")
  void createCourierCreatesDefaultVehicle() {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("courier@example.com");
    request.setPassword("password123");
    request.setName("배송원");
    request.setPhoneNumber("010-1234-5678");
    request.setRole(MemberRole.COURIER);
    request.setVehicleType(VehicleType.MOTORCYCLE);
    request.setActivityRegion("서울 강남구");
    request.setPreferredWeight(15.0);

    when(memberRepository.existsByEmail("courier@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
    when(memberRepository.save(any(Member.class)))
        .thenAnswer(
            invocation -> {
              Member member = invocation.getArgument(0);
              member.setId(10L);
              return member;
            });
    when(vehicleRepository.save(any(Vehicle.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Member response = memberService.create(request);

    assertThat(response.getRole()).isEqualTo(MemberRole.COURIER);
    verify(vehicleRepository).save(any(Vehicle.class));
  }

  @Test
  @DisplayName("이메일이 중복되면 DuplicateMemberException을 던진다")
  void createMemberDuplicateEmailShouldThrowException() {
    MemberCreateRequest request = new MemberCreateRequest();
    request.setEmail("dup@example.com");
    when(memberRepository.existsByEmail("dup@example.com")).thenReturn(true);

    assertThatThrownBy(() -> memberService.create(request))
        .isInstanceOf(DuplicateMemberException.class);
  }

  @Test
  @DisplayName("존재하지 않는 회원을 조회하면 EntityNotFoundException을 던진다")
  void getMemberNotFoundShouldThrowException() {
    when(memberRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> memberService.getById(1L)).isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("존재하는 회원을 조회하면 Member를 반환한다")
  void getMemberSuccess() {
    Member member = new Member();
    member.setEmail("user@example.com");
    member.setName("홍길동");
    member.setPhoneNumber("010-1234-5678");
    member.setRole(MemberRole.COURIER);
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    Member response = memberService.getById(1L);

    assertThat(response.getEmail()).isEqualTo("user@example.com");
  }

  @Test
  @DisplayName("로그인한 본인의 프로필을 조회한다")
  void getMyProfileSuccess() {
    Member member = new Member();
    member.setEmail("my@example.com");
    member.setName("김철수");
    member.setPhoneNumber("010-9876-5432");
    member.setRole(MemberRole.CUSTOMER);
    when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

    Member response = memberService.getMyProfile(2L);

    assertThat(response.getEmail()).isEqualTo("my@example.com");
    assertThat(response.getName()).isEqualTo("김철수");
    assertThat(response.getPhoneNumber()).isEqualTo("010-9876-5432");
  }

  @Test
  @DisplayName("로그인한 본인의 프로필 이름과 연락처를 수정한다")
  void updateMyProfileSuccess() {
    Member member = new Member();
    member.setEmail("my@example.com");
    member.setName("김철수");
    member.setPhoneNumber("010-9876-5432");
    member.setRole(MemberRole.CUSTOMER);
    when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

    MemberProfileUpdateRequestDto request =
        new MemberProfileUpdateRequestDto("김영희", "010-1111-2222");

    Member response = memberService.updateMyProfile(2L, request);

    assertThat(response.getName()).isEqualTo("김영희");
    assertThat(response.getPhoneNumber()).isEqualTo("010-1111-2222");
  }

  @Test
  @DisplayName("배송원 회원 삭제 시 소유 차량을 먼저 정리한다")
  void deleteCourierRemovesOwnedVehiclesFirst() {
    Member member = new Member();
    member.setId(3L);
    when(memberRepository.findById(3L)).thenReturn(Optional.of(member));

    Vehicle vehicle = new Vehicle();
    vehicle.setId(99L);
    when(vehicleRepository.findAllByOwnerId(3L)).thenReturn(List.of(vehicle));

    memberService.delete(3L);

    verify(vehicleRepository).delete(vehicle);
    verify(memberRepository).delete(member);
  }
}

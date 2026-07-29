package com.example.shinhangaecheokja.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest;
import com.example.shinhangaecheokja.member.dto.response.MemberResponse;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberRoleUpdateTest {

  @Autowired private MemberService memberService;

  @Test
  @DisplayName("회원의 역할을 CUSTOMER에서 COURIER로 정상 변경한다.")
  void updateRoleToCourier() {
    // given
    MemberCreateRequest createRequest = new MemberCreateRequest();
    createRequest.setEmail("role_test@example.com");
    createRequest.setPassword("password123!");
    createRequest.setName("홍길동");
    createRequest.setPhoneNumber("010-1234-5678");
    createRequest.setRole(MemberRole.CUSTOMER);

    MemberResponse created = memberService.createMember(createRequest);
    assertThat(created.role()).isEqualTo(MemberRole.CUSTOMER);

    // when
    MemberResponse updated = memberService.updateRole(created.id(), MemberRole.COURIER);

    // then
    assertThat(updated.role()).isEqualTo(MemberRole.COURIER);
  }
}

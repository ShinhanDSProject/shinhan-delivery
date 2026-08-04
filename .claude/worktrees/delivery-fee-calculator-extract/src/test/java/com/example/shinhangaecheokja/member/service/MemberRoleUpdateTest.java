package com.example.shinhandelivery.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.member.dto.request.MemberCreateRequest;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
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

    Member created = memberService.create(createRequest);
    assertThat(created.getRole()).isEqualTo(MemberRole.CUSTOMER);

    // when
    Member updated = memberService.updateRole(created.getId(), MemberRole.COURIER);

    // then
    assertThat(updated.getRole()).isEqualTo(MemberRole.COURIER);
  }

  @Test
  @DisplayName("역할 변경 시 ADMIN 권한으로의 직접 변경은 차단된다.")
  void updateRoleToAdminThrowsException() {
    // given
    MemberCreateRequest createRequest = new MemberCreateRequest();
    createRequest.setEmail("role_admin_test@example.com");
    createRequest.setPassword("password123!");
    createRequest.setName("홍길동");
    createRequest.setPhoneNumber("010-1234-5678");
    createRequest.setRole(MemberRole.CUSTOMER);

    Member created = memberService.create(createRequest);

    // when & then
    assertThatThrownBy(() -> memberService.updateRole(created.getId(), MemberRole.ADMIN))
        .isInstanceOf(BusinessException.class);
  }
}

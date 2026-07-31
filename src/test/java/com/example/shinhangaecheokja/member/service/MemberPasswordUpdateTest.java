package com.example.shinhangaecheokja.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.common.security.JwtProvider;
import com.example.shinhangaecheokja.member.dto.request.MemberPasswordUpdateRequest;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberPasswordUpdateTest {

  @Mock private MemberRepository memberRepository;
  @Spy private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  @Mock private JwtProvider jwtProvider;
  @InjectMocks private MemberService memberService;

  private Member member;

  @BeforeEach
  void setUp() {
    member =
        Member.builder()
            .id(1L)
            .email("user@example.com")
            .password(passwordEncoder.encode("OldPassword1!"))
            .name("홍길동")
            .phoneNumber("010-1234-5678")
            .role(MemberRole.CUSTOMER)
            .build();
    given(memberRepository.findById(1L)).willReturn(Optional.of(member));
  }

  @Test
  @DisplayName("현재 비밀번호가 맞으면 새 비밀번호를 암호화해 저장한다")
  void updatePasswordSuccess() {
    MemberPasswordUpdateRequest request =
        new MemberPasswordUpdateRequest("OldPassword1!", "NewPassword2@", "NewPassword2@");

    memberService.updatePassword(1L, request);

    assertThat(passwordEncoder.matches("NewPassword2@", member.getPassword())).isTrue();
    assertThat(member.getPassword()).isNotEqualTo("NewPassword2@");
  }

  @Test
  @DisplayName("현재 비밀번호가 틀리면 변경하지 않는다")
  void updatePasswordWrongCurrentPasswordShouldThrowException() {
    MemberPasswordUpdateRequest request =
        new MemberPasswordUpdateRequest("WrongPassword1!", "NewPassword2@", "NewPassword2@");

    assertThatThrownBy(() -> memberService.updatePassword(1L, request))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.CURRENT_PASSWORD_MISMATCH);
  }

  @Test
  @DisplayName("새 비밀번호와 확인이 다르면 변경하지 않는다")
  void updatePasswordMismatchNewPasswordShouldThrowException() {
    MemberPasswordUpdateRequest request =
        new MemberPasswordUpdateRequest("OldPassword1!", "NewPassword2@", "Different3#");

    assertThatThrownBy(() -> memberService.updatePassword(1L, request))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
  }

  @Test
  @DisplayName("현재와 동일한 비밀번호는 다시 사용할 수 없다")
  void updatePasswordSamePasswordReuseShouldThrowException() {
    MemberPasswordUpdateRequest request =
        new MemberPasswordUpdateRequest("OldPassword1!", "OldPassword1!", "OldPassword1!");

    assertThatThrownBy(() -> memberService.updatePassword(1L, request))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.PASSWORD_REUSE_NOT_ALLOWED);
  }
}

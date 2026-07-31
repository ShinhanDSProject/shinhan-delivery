package com.example.shinhangaecheokja.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 엔티티. role로 Customer/Courier/Admin을 구분한다. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member")
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  @Column(nullable = false, length = 100)
  private String password;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(name = "phone_number", nullable = false, length = 20)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MemberRole role;

  /** 회원의 역할(CUSTOMER/COURIER)을 변경한다. ADMIN 권한 승격은 직접 변경을 불허한다. */
  public void changeRole(MemberRole newRole) {
    if (newRole == null) {
      throw new IllegalArgumentException("변경할 역할은 필수 선택 항목입니다.");
    }
    if (newRole == MemberRole.ADMIN) {
      throw new com.example.shinhangaecheokja.common.exception.BusinessException(
          com.example.shinhangaecheokja.common.exception.ErrorCode.INVALID_INPUT_VALUE);
    }
    this.role = newRole;
  }

  /** 암호화된 새 비밀번호로 회원 비밀번호를 변경한다. */
  public void changePassword(String encodedPassword) {
    if (encodedPassword == null || encodedPassword.isBlank()) {
      throw new IllegalArgumentException("암호화된 비밀번호는 필수입니다.");
    }
    this.password = encodedPassword;
  }

  /** MemberCreateRequest DTO 기반으로 Member 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Member from(
      com.example.shinhangaecheokja.member.dto.request.MemberCreateRequest request,
      String encodedPassword) {
    return Member.builder()
        .email(request.getEmail())
        .password(encodedPassword)
        .name(request.getName())
        .phoneNumber(request.getPhoneNumber())
        .role(request.getRole())
        .build();
  }
}

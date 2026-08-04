package com.example.shinhandelivery.member.entity;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.dto.request.MemberCreateRequest;
import com.example.shinhandelivery.member.dto.request.MemberProfileUpdateRequestDto;
import com.example.shinhandelivery.member.dto.request.MemberUpdateRequest;
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

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, length = 255)
  private String password;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(name = "phone_number", nullable = false, length = 20)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MemberRole role;

  /** 회원의 역할(CUSTOMER/COURIER)을 변경한다. ADMIN 권한 승격은 직접 변경을 불허한다. */
  public Member changeRole(MemberRole newRole) {
    if (newRole == null) {
      throw new IllegalArgumentException("변경할 역할은 필수 선택 항목입니다.");
    }
    if (newRole == MemberRole.ADMIN) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
    this.role = newRole;
    return this;
  }

  /** 암호화된 새 비밀번호로 회원 비밀번호를 변경한다. */
  public Member changePassword(String encodedPassword) {
    if (encodedPassword == null || encodedPassword.isBlank()) {
      throw new IllegalArgumentException("암호화된 비밀번호는 필수입니다.");
    }
    this.password = encodedPassword;
    return this;
  }

  /** MemberUpdateRequest DTO 기반으로 회원 이름·연락처를 수정하는 도메인 비즈니스 메서드. */
  public Member updateBy(MemberUpdateRequest request) {
    this.name = request.getName();
    this.phoneNumber = request.getPhoneNumber();
    return this;
  }

  /** MemberProfileUpdateRequestDto DTO 기반으로 회원 프로필 정보(2이름·연락처)를 수정하는 도메인 비즈니스 메서드. */
  public Member updateProfileBy(MemberProfileUpdateRequestDto request) {
    this.name = request.getName();
    this.phoneNumber = request.getPhoneNumber();
    return this;
  }

  /** MemberCreateRequest DTO 기반으로 Member 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Member from(MemberCreateRequest request, String encodedPassword) {
    return Member.builder()
        .email(request.getEmail())
        .password(encodedPassword)
        .name(request.getName())
        .phoneNumber(request.getPhoneNumber())
        .role(request.getRole())
        .build();
  }
}

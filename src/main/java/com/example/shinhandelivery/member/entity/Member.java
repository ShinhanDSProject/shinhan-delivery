package com.example.shinhandelivery.member.entity;

import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.member.dto.request.MemberCreateRequest;
import com.example.shinhandelivery.member.dto.request.MemberProfileUpdateRequest;
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

  @Column(name = "activity_region", length = 100)
  private String activityRegion;

  @Column(name = "preferred_weight")
  private Double preferredWeight;

  @Column(name = "pin_hash", length = 255)
  private String pinHash;

  @Column(name = "pin_fail_count", nullable = false)
  private int pinFailCount;

  @Column(name = "pin_locked", nullable = false)
  private boolean pinLocked;

  @Enumerated(EnumType.STRING)
  @Column(name = "courier_approval_status", nullable = false, length = 20)
  @Builder.Default
  private CourierApprovalStatus courierApprovalStatus = CourierApprovalStatus.APPROVED;

  @Column(name = "proof_document_url", length = 255)
  private String proofDocumentUrl;

  /** 회원의 역할(CUSTOMER/COURIER)을 변경한다. ADMIN 권한 승격은 직접 변경을 불허한다. */
  public Member changeRole(MemberRole newRole) {
    if (newRole == null) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
    if (newRole == MemberRole.ADMIN) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
    if (this.role != MemberRole.COURIER && newRole == MemberRole.COURIER) {
      this.courierApprovalStatus = CourierApprovalStatus.PENDING;
    }
    this.role = newRole;
    return this;
  }

  /** 배송원의 자격 심사를 최종 승인한다. */
  public Member approveCourier() {
    if (this.proofDocumentUrl == null || this.proofDocumentUrl.isBlank()) {
      throw new BusinessException(ErrorCode.COURIER_PROOF_REQUIRED);
    }
    this.courierApprovalStatus = CourierApprovalStatus.APPROVED;
    return this;
  }

  /** 배송원의 자격 심사를 거절한다. */
  public Member rejectCourier() {
    this.courierApprovalStatus = CourierApprovalStatus.REJECTED;
    return this;
  }

  /** 배송원 증빙 서류 이미지 URL을 등록/수정한다. (거절 상태였다면 PENDING으로 재심사 전환) */
  public Member updateProofDocumentUrl(String proofDocumentUrl) {
    this.proofDocumentUrl = proofDocumentUrl;
    if (this.role == MemberRole.COURIER
        && this.courierApprovalStatus == CourierApprovalStatus.REJECTED) {
      this.courierApprovalStatus = CourierApprovalStatus.PENDING;
    }
    return this;
  }

  /** 암호화된 새 비밀번호로 회원 비밀번호를 변경한다. */
  public Member changePassword(String encodedPassword) {
    if (encodedPassword == null || encodedPassword.isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
    this.password = encodedPassword;
    return this;
  }

  /** MemberUpdateRequest DTO 기반으로 회원 이름과 연락처를 수정한다. */
  public Member updateBy(MemberUpdateRequest request) {
    this.name = request.getName();
    this.phoneNumber = request.getPhoneNumber();
    return this;
  }

  /** MemberProfileUpdateRequest DTO 기반으로 회원 프로필 정보를 수정한다. */
  public Member updateProfileBy(MemberProfileUpdateRequest request) {
    this.name = request.getName();
    this.phoneNumber = request.getPhoneNumber();
    return this;
  }

  /** PIN 검증 성공 시 실패 횟수를 초기화한다. */
  public Member resetPinFailures() {
    this.pinFailCount = 0;
    return this;
  }

  /** PIN 검증 실패를 기록하고 3회 이상이면 잠금 처리한다. */
  public Member recordPinFailure() {
    this.pinFailCount += 1;
    if (this.pinFailCount >= 3) {
      this.pinLocked = true;
    }
    return this;
  }

  /** 결제 PIN을 저장하고 잠금/실패 횟수를 초기화한다. */
  public Member changePaymentPin(String encodedPin) {
    if (encodedPin == null || encodedPin.isBlank()) {
      throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
    }
    this.pinHash = encodedPin;
    this.pinFailCount = 0;
    this.pinLocked = false;
    return this;
  }

  /** MemberCreateRequest DTO 기반으로 Member 엔티티를 생성하는 정적 팩토리 메서드. */
  public static Member from(MemberCreateRequest request, String encodedPassword) {
    CourierApprovalStatus initialApprovalStatus =
        request.getRole() == MemberRole.COURIER
            ? CourierApprovalStatus.PENDING
            : CourierApprovalStatus.APPROVED;

    return Member.builder()
        .email(request.getEmail())
        .password(encodedPassword)
        .name(request.getName())
        .phoneNumber(request.getPhoneNumber())
        .role(request.getRole())
        .activityRegion(request.getActivityRegion())
        .preferredWeight(request.getPreferredWeight())
        .courierApprovalStatus(initialApprovalStatus)
        .proofDocumentUrl(request.getProofDocumentUrl())
        .build();
  }
}

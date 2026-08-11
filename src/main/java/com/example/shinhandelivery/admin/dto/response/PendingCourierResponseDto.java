package com.example.shinhandelivery.admin.dto.response;

import com.example.shinhandelivery.member.entity.CourierApprovalStatus;
import com.example.shinhandelivery.member.entity.Member;

/** 관리자 전용 승인 대기 배송원 정보 응답 DTO. */
public record PendingCourierResponseDto(
    Long id,
    String email,
    String name,
    String phoneNumber,
    String activityRegion,
    Double preferredWeight,
    CourierApprovalStatus courierApprovalStatus,
    String proofDocumentUrl) {

  /** Member 엔티티를 PendingCourierResponseDto로 변환한다. */
  public static PendingCourierResponseDto from(Member member) {
    return new PendingCourierResponseDto(
        member.getId(),
        member.getEmail(),
        member.getName(),
        member.getPhoneNumber(),
        member.getActivityRegion(),
        member.getPreferredWeight(),
        member.getCourierApprovalStatus(),
        member.getProofDocumentUrl());
  }
}

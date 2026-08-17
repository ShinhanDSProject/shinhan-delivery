package com.example.shinhandelivery.admin.dto.response;

import com.example.shinhandelivery.member.entity.CourierApprovalStatus;
import com.example.shinhandelivery.member.entity.Member;
import lombok.Builder;

/** 관리자 전용 승인 대기 배송원 정보 응답 DTO. */
@Builder
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
    return PendingCourierResponseDto.builder()
        .id(member.getId())
        .email(member.getEmail())
        .name(member.getName())
        .phoneNumber(member.getPhoneNumber())
        .activityRegion(member.getActivityRegion())
        .preferredWeight(member.getPreferredWeight())
        .courierApprovalStatus(member.getCourierApprovalStatus())
        .proofDocumentUrl(member.getProofDocumentUrl())
        .build();
  }
}

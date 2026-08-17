package com.example.shinhandelivery.member.dto.response;

import com.example.shinhandelivery.member.entity.CourierApprovalStatus;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import lombok.Builder;

/** 회원 본인 프로필 조회 응답 DTO. 비밀번호는 노출하지 않는다. */
@Builder
public record MemberProfileResponse(
    Long id,
    String email,
    String name,
    String phoneNumber,
    MemberRole role,
    boolean hasPaymentPin,
    CourierApprovalStatus courierApprovalStatus,
    String proofDocumentUrl) {

  /** Member 엔티티를 MemberProfileResponse로 변환한다. */
  public static MemberProfileResponse from(Member entity) {
    return MemberProfileResponse.builder()
        .id(entity.getId())
        .email(entity.getEmail())
        .name(entity.getName())
        .phoneNumber(entity.getPhoneNumber())
        .role(entity.getRole())
        .hasPaymentPin(entity.getPinHash() != null && !entity.getPinHash().isBlank())
        .courierApprovalStatus(entity.getCourierApprovalStatus())
        .proofDocumentUrl(entity.getProofDocumentUrl())
        .build();
  }
}

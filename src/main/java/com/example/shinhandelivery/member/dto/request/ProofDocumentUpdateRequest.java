package com.example.shinhandelivery.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 배송원 자격 증빙 서류 이미지 URL 등록/수정 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class ProofDocumentUpdateRequest {

  @NotBlank(message = "증빙 서류 이미지 URL은 필수입니다.")
  private String proofDocumentUrl;

  public ProofDocumentUpdateRequest(String proofDocumentUrl) {
    this.proofDocumentUrl = proofDocumentUrl;
  }
}

package com.example.shinhandelivery.notice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 관리자 공지사항 생성 요청 DTO입니다. */
@Schema(description = "관리자 공지사항 생성 요청")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NoticeCreateRequest {

  private static final String CATEGORY_PATTERN = "^\\s*(SYSTEM|EVENT|SERVICE|ANNOUNCEMENT)\\s*$";

  @Schema(description = "공지사항 제목", example = "[안내] 서비스 정기 점검 안내")
  @NotBlank(message = "공지사항 제목은 필수입니다.")
  @Size(max = 150, message = "공지사항 제목은 150자 이하여야 합니다.")
  private String title;

  @Schema(description = "공지사항 본문", example = "서비스 점검 일정을 안내드립니다.")
  @NotBlank(message = "공지사항 본문은 필수입니다.")
  private String content;

  @Schema(description = "공지사항 카테고리", example = "SYSTEM")
  @NotBlank(message = "공지사항 카테고리는 필수입니다.")
  @Pattern(
      regexp = CATEGORY_PATTERN,
      message = "카테고리는 SYSTEM, EVENT, SERVICE, ANNOUNCEMENT 중 하나여야 합니다.")
  private String category;

  @Schema(description = "상단 고정 여부", example = "true")
  @NotNull(message = "상단 고정 여부는 필수입니다.")
  private Boolean isPinned;

  public static NoticeCreateRequest of(
      String title, String content, String category, Boolean isPinned) {
    return new NoticeCreateRequest(title, content, category, isPinned);
  }
}

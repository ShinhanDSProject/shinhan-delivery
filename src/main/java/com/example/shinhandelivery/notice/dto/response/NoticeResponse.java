package com.example.shinhandelivery.notice.dto.response;

import com.example.shinhandelivery.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

/** 공지사항 목록 요약 응답 DTO 레코드입니다. */
@Schema(description = "공지사항 목록 요약 응답")
@Builder
public record NoticeResponse(
    @Schema(description = "공지사항 ID", example = "1") Long id,
    @Schema(description = "제목", example = "[안내] 서비스 정기 점검 안내") String title,
    @Schema(description = "본문", example = "정기 점검으로 인해 서비스 이용이 일시 중단됩니다.") String content,
    @Schema(description = "카테고리", example = "SYSTEM") String category,
    @Schema(description = "상단 고정 여부", example = "true") Boolean isPinned,
    @Schema(description = "작성 일시", example = "2026-07-30T09:00:00") LocalDateTime createdAt,
    @Schema(description = "수정 일시", example = "2026-07-30T09:00:00") LocalDateTime updatedAt) {

  public static NoticeResponse from(Notice notice) {
    return NoticeResponse.builder()
        .id(notice.getId())
        .title(notice.getTitle())
        .content(notice.getContent())
        .category(notice.getCategory())
        .isPinned(notice.getIsPinned())
        .createdAt(notice.getCreatedAt())
        .updatedAt(notice.getUpdatedAt())
        .build();
  }
}

package com.example.shinhangaecheokja.notice.dto.response;

import com.example.shinhangaecheokja.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/** 공지사항 상세 응답 DTO 레코드입니다. */
@Schema(description = "공지사항 상세 응답")
public record NoticeDetailResponse(
    @Schema(description = "공지사항 ID", example = "1") Long id,
    @Schema(description = "제목", example = "[안내] 서비스 정기 점검 안내") String title,
    @Schema(description = "상세 본문 내용", example = "안녕하세요. 정기 점검 안내입니다...") String content,
    @Schema(description = "카테고리", example = "SYSTEM") String category,
    @Schema(description = "상단 고정 여부", example = "true") Boolean isPinned,
    @Schema(description = "작성 일시", example = "2026-07-30T09:00:00") LocalDateTime createdAt,
    @Schema(description = "수정 일시", example = "2026-07-30T09:00:00") LocalDateTime updatedAt) {

  public static NoticeDetailResponse from(Notice notice) {
    return new NoticeDetailResponse(
        notice.getId(),
        notice.getTitle(),
        notice.getContent(),
        notice.getCategory(),
        notice.getIsPinned(),
        notice.getCreatedAt(),
        notice.getUpdatedAt());
  }
}

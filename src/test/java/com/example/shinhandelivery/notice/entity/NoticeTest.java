package com.example.shinhandelivery.notice.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.notice.dto.request.NoticeCreateRequest;
import com.example.shinhandelivery.notice.dto.request.NoticeUpdateRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoticeTest {

  @Test
  @DisplayName("생성 요청을 공지사항으로 변환하며 문자열 공백과 시간을 정규화한다")
  void createFromRequestSuccess() {
    LocalDateTime before = LocalDateTime.now();

    Notice notice = Notice.from(NoticeCreateRequest.of("  제목  ", "  본문  ", " SYSTEM ", true));

    assertThat(notice.getTitle()).isEqualTo("제목");
    assertThat(notice.getContent()).isEqualTo("본문");
    assertThat(notice.getCategory()).isEqualTo("SYSTEM");
    assertThat(notice.getIsPinned()).isTrue();
    assertThat(notice.getCreatedAt()).isAfterOrEqualTo(before);
    assertThat(notice.getUpdatedAt()).isEqualTo(notice.getCreatedAt());
  }

  @Test
  @DisplayName("수정 요청은 변경 가능 필드와 수정일만 바꾼다")
  void updateByRequestPreservesIdentityAndCreatedAt() {
    LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
    LocalDateTime previousUpdatedAt = createdAt.plusHours(1);
    Notice notice =
        Notice.builder()
            .id(1L)
            .title("이전 제목")
            .content("이전 본문")
            .category("EVENT")
            .isPinned(false)
            .createdAt(createdAt)
            .updatedAt(previousUpdatedAt)
            .build();

    Notice updated =
        notice.updateBy(NoticeUpdateRequest.of("  새 제목 ", " 새 본문 ", " SERVICE ", true));

    assertThat(updated).isSameAs(notice);
    assertThat(updated.getId()).isEqualTo(1L);
    assertThat(updated.getTitle()).isEqualTo("새 제목");
    assertThat(updated.getContent()).isEqualTo("새 본문");
    assertThat(updated.getCategory()).isEqualTo("SERVICE");
    assertThat(updated.getIsPinned()).isTrue();
    assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
    assertThat(updated.getUpdatedAt()).isAfter(previousUpdatedAt);
  }
}

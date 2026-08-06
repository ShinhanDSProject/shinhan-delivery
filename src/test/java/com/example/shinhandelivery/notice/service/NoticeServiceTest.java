package com.example.shinhandelivery.notice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.notice.dto.request.NoticeCreateRequest;
import com.example.shinhandelivery.notice.dto.request.NoticeUpdateRequest;
import com.example.shinhandelivery.notice.entity.Notice;
import com.example.shinhandelivery.notice.exception.NoticeNotFoundException;
import com.example.shinhandelivery.notice.repository.NoticeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

  @Mock private NoticeRepository noticeRepository;

  @InjectMocks private NoticeService noticeService;

  @Test
  @DisplayName("카테고리가 없으면 전체 공지사항을 상단고정 및 최신순으로 조회한다")
  void getNoticesWithoutCategory() {
    Pageable pageable = PageRequest.of(0, 10);
    Notice notice = createNotice(1L, "제목", "내용", "SYSTEM", true);
    when(noticeRepository.findAllByOrderByIsPinnedDescCreatedAtDesc(pageable))
        .thenReturn(new PageImpl<>(List.of(notice)));

    Page<Notice> responses = noticeService.list(null, pageable);

    assertThat(responses.getContent()).hasSize(1);
    assertThat(responses.getContent().get(0).getTitle()).isEqualTo("제목");
    verify(noticeRepository, never()).findByCategoryOrderByIsPinnedDescCreatedAtDesc(any(), any());
  }

  @Test
  @DisplayName("카테고리가 주어지면 해당 카테고리의 공지사항만 필터링하여 조회한다")
  void getNoticesWithCategory() {
    Pageable pageable = PageRequest.of(0, 10);
    Notice notice = createNotice(2L, "이벤트 제목", "내용", "EVENT", false);
    when(noticeRepository.findByCategoryOrderByIsPinnedDescCreatedAtDesc("EVENT", pageable))
        .thenReturn(new PageImpl<>(List.of(notice)));

    Page<Notice> responses = noticeService.list("EVENT", pageable);

    assertThat(responses.getContent()).hasSize(1);
    assertThat(responses.getContent().get(0).getCategory()).isEqualTo("EVENT");
  }

  @Test
  @DisplayName("존재하는 공지사항 ID로 상세 조회 시 상세 정보를 반환한다")
  void getNoticeDetailSuccess() {
    Notice notice = createNotice(1L, "상세 제목", "상세 본문 내용", "SYSTEM", true);
    when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

    Notice response = noticeService.getById(1L);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getTitle()).isEqualTo("상세 제목");
    assertThat(response.getContent()).isEqualTo("상세 본문 내용");
  }

  @Test
  @DisplayName("존재하지 않는 공지사항 ID로 상세 조회 시 NoticeNotFoundException 예외를 던진다")
  void getNoticeDetailNotFound() {
    when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> noticeService.getById(999L))
        .isInstanceOf(NoticeNotFoundException.class);
  }

  @Test
  @DisplayName("관리자 생성 요청으로 공지사항을 저장한다")
  void createNoticeSuccess() {
    NoticeCreateRequest request = NoticeCreateRequest.of("제목", "본문", "SYSTEM", true);
    when(noticeRepository.save(any(Notice.class)))
        .thenAnswer(
            invocation -> {
              Notice notice = invocation.getArgument(0);
              return Notice.builder()
                  .id(1L)
                  .title(notice.getTitle())
                  .content(notice.getContent())
                  .category(notice.getCategory())
                  .isPinned(notice.getIsPinned())
                  .createdAt(notice.getCreatedAt())
                  .updatedAt(notice.getUpdatedAt())
                  .build();
            });

    Notice created = noticeService.create(request);

    assertThat(created.getId()).isEqualTo(1L);
    assertThat(created.getTitle()).isEqualTo("제목");
    verify(noticeRepository).save(any(Notice.class));
  }

  @Test
  @DisplayName("관리자 수정 요청으로 기존 공지사항의 내용을 변경한다")
  void updateNoticeSuccess() {
    Notice notice = createNotice(1L, "이전 제목", "이전 본문", "EVENT", false);
    LocalDateTime createdAt = notice.getCreatedAt();
    when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
    NoticeUpdateRequest request = NoticeUpdateRequest.of("새 제목", "새 본문", "SYSTEM", true);

    Notice updated = noticeService.update(1L, request);

    assertThat(updated.getTitle()).isEqualTo("새 제목");
    assertThat(updated.getContent()).isEqualTo("새 본문");
    assertThat(updated.getCategory()).isEqualTo("SYSTEM");
    assertThat(updated.getIsPinned()).isTrue();
    assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  @DisplayName("존재하지 않는 공지사항 수정 시 NoticeNotFoundException을 던진다")
  void updateNoticeNotFound() {
    when(noticeRepository.findById(999L)).thenReturn(Optional.empty());
    NoticeUpdateRequest request = NoticeUpdateRequest.of("제목", "본문", "SYSTEM", false);

    assertThatThrownBy(() -> noticeService.update(999L, request))
        .isInstanceOf(NoticeNotFoundException.class);
  }

  @Test
  @DisplayName("관리자 삭제 요청으로 기존 공지사항을 삭제한다")
  void deleteNoticeSuccess() {
    Notice notice = createNotice(1L, "제목", "본문", "SYSTEM", false);
    when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

    noticeService.delete(1L);

    verify(noticeRepository).delete(notice);
  }

  @Test
  @DisplayName("존재하지 않는 공지사항 삭제 시 NoticeNotFoundException을 던진다")
  void deleteNoticeNotFound() {
    when(noticeRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> noticeService.delete(999L))
        .isInstanceOf(NoticeNotFoundException.class);
  }

  private Notice createNotice(
      Long id, String title, String content, String category, boolean isPinned) {
    return Notice.builder()
        .id(id)
        .title(title)
        .content(content)
        .category(category)
        .isPinned(isPinned)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}

package com.example.shinhangaecheokja.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.notification.entity.Notification;
import com.example.shinhangaecheokja.notification.exception.NotificationAccessDeniedException;
import com.example.shinhangaecheokja.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;
  @InjectMocks private NotificationService notificationService;

  @Test
  void category가_없으면_전체_알림을_최신순으로_조회한다() {
    Pageable pageable = PageRequest.of(0, 10);
    Notification notification = newNotification(1L, 1L, "DELIVERY", false);
    when(notificationRepository.findByMemberIdOrderByCreatedAtDesc(1L, pageable))
        .thenReturn(new PageImpl<>(java.util.List.of(notification)));

    var responses = notificationService.getNotifications(1L, null, pageable);

    assertThat(responses.getContent()).hasSize(1);
    verify(notificationRepository, never())
        .findByMemberIdAndCategoryOrderByCreatedAtDesc(any(), any(), any());
  }

  @Test
  void category가_있으면_해당_카테고리만_조회한다() {
    Pageable pageable = PageRequest.of(0, 10);
    Notification notification = newNotification(1L, 1L, "DELIVERY", false);
    when(notificationRepository.findByMemberIdAndCategoryOrderByCreatedAtDesc(
            1L, "DELIVERY", pageable))
        .thenReturn(new PageImpl<>(java.util.List.of(notification)));

    var responses = notificationService.getNotifications(1L, "DELIVERY", pageable);

    assertThat(responses.getContent()).hasSize(1);
    assertThat(responses.getContent().get(0).getCategory()).isEqualTo("DELIVERY");
  }

  @Test
  void 본인_알림을_읽음_처리한다() {
    Notification notification = newNotification(1L, 1L, "DELIVERY", false);
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

    Notification response = notificationService.markAsRead(1L, 1L);

    assertThat(response.isRead()).isTrue();
  }

  @Test
  void 존재하지_않는_알림이면_EntityNotFoundException을_던진다() {
    when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.markAsRead(999L, 1L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void 본인_알림이_아니면_NotificationAccessDeniedException을_던진다() {
    Notification notification = newNotification(1L, 1L, "DELIVERY", false);
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

    assertThatThrownBy(() -> notificationService.markAsRead(1L, 2L))
        .isInstanceOf(NotificationAccessDeniedException.class);
    assertThat(notification.isRead()).isFalse();
  }

  private Notification newNotification(Long id, Long memberId, String category, boolean read) {
    Notification notification = new Notification();
    notification.setId(id);
    notification.setMemberId(memberId);
    notification.setTitle("제목");
    notification.setMessage("내용");
    notification.setCategory(category);
    notification.setRead(read);
    notification.setCreatedAt(LocalDateTime.now());
    return notification;
  }
}

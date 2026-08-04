package com.example.shinhandelivery.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.notification.entity.Notification;
import com.example.shinhandelivery.notification.exception.NotificationAccessDeniedException;
import com.example.shinhandelivery.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("category가 없으면 전체 알림을 최신순으로 조회한다")
  void getNotificationsWithoutCategoryShouldReturnAll() {
    Pageable pageable = PageRequest.of(0, 10);
    Notification notification = newNotification(1L, 1L, "DELIVERY", false);
    when(notificationRepository.findByMemberIdOrderByCreatedAtDesc(1L, pageable))
        .thenReturn(new PageImpl<>(java.util.List.of(notification)));

    var responses = notificationService.list(1L, null, pageable);

    assertThat(responses.getContent()).hasSize(1);
    verify(notificationRepository, never())
        .findByMemberIdAndCategoryOrderByCreatedAtDesc(any(), any(), any());
  }

  @Test
  @DisplayName("category가 있으면 해당 카테고리만 조회한다")
  void getNotificationsWithCategoryShouldFilterByCategory() {
    Pageable pageable = PageRequest.of(0, 10);
    Notification notification = newNotification(1L, 1L, "DELIVERY", false);
    when(notificationRepository.findByMemberIdAndCategoryOrderByCreatedAtDesc(
            1L, "DELIVERY", pageable))
        .thenReturn(new PageImpl<>(java.util.List.of(notification)));

    var responses = notificationService.list(1L, "DELIVERY", pageable);

    assertThat(responses.getContent()).hasSize(1);
    assertThat(responses.getContent().get(0).getCategory()).isEqualTo("DELIVERY");
  }

  @Test
  @DisplayName("본인 알림을 읽음 처리한다")
  void markAsReadSuccess() {
    Notification notification = newNotification(1L, 1L, "DELIVERY", false);
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

    Notification response = notificationService.markAsRead(1L, 1L);

    assertThat(response.isRead()).isTrue();
  }

  @Test
  @DisplayName("존재하지 않는 알림이면 EntityNotFoundException을 던진다")
  void markAsReadNotFoundShouldThrowException() {
    when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> notificationService.markAsRead(1L, 999L))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  @DisplayName("본인 알림이 아니면 NotificationAccessDeniedException을 던진다")
  void markAsReadAccessDeniedShouldThrowException() {
    Notification notification = newNotification(1L, 1L, "DELIVERY", false);
    when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

    assertThatThrownBy(() -> notificationService.markAsRead(2L, 1L))
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

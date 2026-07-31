package com.example.shinhangaecheokja.notification.service;

import com.example.shinhangaecheokja.common.exception.EntityNotFoundException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.notification.entity.Notification;
import com.example.shinhangaecheokja.notification.exception.NotificationAccessDeniedException;
import com.example.shinhangaecheokja.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Notification 관련 유스케이스(목록 조회/읽음 처리)를 담당하는 서비스. */
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  /** 로그인 회원 본인의 알림만 최신순으로 페이징 조회한다. category가 있으면 그 카테고리로 필터링한다. */
  @Transactional(readOnly = true)
  public Page<Notification> getNotifications(Long memberId, String category, Pageable pageable) {
    return StringUtils.hasText(category)
        ? notificationRepository.findByMemberIdAndCategoryOrderByCreatedAtDesc(
            memberId, category, pageable)
        : notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
  }

  /** 알림을 읽음 처리한다. 없으면 EntityNotFoundException, 본인 알림이 아니면 NotificationAccessDeniedException. */
  @Transactional
  public Notification markAsRead(Long notificationId, Long memberId) {
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));

    if (!notification.getMemberId().equals(memberId)) {
      throw new NotificationAccessDeniedException(notificationId, memberId);
    }

    notification.setRead(true);
    return notification;
  }
}

package com.example.shinhandelivery.notification.repository;

import com.example.shinhandelivery.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Notification 엔티티에 대한 JPA 저장소. */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  /** 회원의 전체 알림을 최신순으로 페이징 조회한다. */
  Page<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

  /** 회원의 특정 카테고리 알림만 최신순으로 페이징 조회한다. */
  Page<Notification> findByMemberIdAndCategoryOrderByCreatedAtDesc(
      Long memberId, String category, Pageable pageable);
}

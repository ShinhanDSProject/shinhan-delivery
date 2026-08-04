package com.example.shinhandelivery.notification.dto.response;

import com.example.shinhandelivery.notification.entity.Notification;
import java.time.LocalDateTime;

/** 알림 응답 DTO. */
public record NotificationResponse(
    Long id, String title, String message, String category, boolean read, LocalDateTime createdAt) {

  /** Notification 엔티티를 응답 DTO로 변환한다. */
  public static NotificationResponse from(Notification entity) {
    return new NotificationResponse(
        entity.getId(),
        entity.getTitle(),
        entity.getMessage(),
        entity.getCategory(),
        entity.isRead(),
        entity.getCreatedAt());
  }
}

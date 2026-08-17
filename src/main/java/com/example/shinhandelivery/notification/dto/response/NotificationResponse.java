package com.example.shinhandelivery.notification.dto.response;

import com.example.shinhandelivery.notification.entity.Notification;
import java.time.LocalDateTime;
import lombok.Builder;

/** 알림 응답 DTO. */
@Builder
public record NotificationResponse(
    Long id, String title, String message, String category, boolean read, LocalDateTime createdAt) {

  /** Notification 엔티티를 응답 DTO로 변환한다. */
  public static NotificationResponse from(Notification entity) {
    return NotificationResponse.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .message(entity.getMessage())
        .category(entity.getCategory())
        .read(entity.isRead())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}

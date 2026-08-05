package com.example.shinhandelivery.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 알림 엔티티. member_id는 Member를 가리키는 FK 값이다. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, length = 500)
  private String message;

  @Column(nullable = false, length = 50)
  private String category;

  @Column(name = "is_read", nullable = false)
  private boolean isRead;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  /** 카테고리별 알림을 생성하는 정적 팩토리 메서드. */
  public static Notification of(
      Long memberId, String title, String message, String category, boolean isRead) {
    return Notification.builder()
        .memberId(memberId)
        .title(title)
        .message(message)
        .category(category)
        .isRead(isRead)
        .createdAt(LocalDateTime.now())
        .build();
  }

  /** 알림을 읽음 처리하는 도메인 비즈니스 메서드. */
  public Notification markAsRead() {
    this.isRead = true;
    return this;
  }
}

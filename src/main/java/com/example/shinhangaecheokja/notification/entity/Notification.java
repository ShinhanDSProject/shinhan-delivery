package com.example.shinhangaecheokja.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 알림 엔티티. member_id는 Member를 가리키는 FK 값이다. */
@Entity
@Getter
@Setter
@NoArgsConstructor
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
}

package com.example.shinhandelivery.notice.entity;

import com.example.shinhandelivery.notice.dto.request.NoticeCreateRequest;
import com.example.shinhandelivery.notice.dto.request.NoticeUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 공지사항 정보 엔티티 클래스입니다. */
@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false, length = 50)
  private String category;

  @Column(nullable = false)
  private Boolean isPinned;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  /** 관리자 생성 요청을 공지사항 엔티티로 변환합니다. */
  public static Notice from(NoticeCreateRequest request) {
    LocalDateTime now = LocalDateTime.now();
    return Notice.builder()
        .title(request.getTitle().trim())
        .content(request.getContent().trim())
        .category(request.getCategory().trim())
        .isPinned(request.getIsPinned())
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  /** 관리자 수정 요청을 반영하고 최초 생성일시는 유지합니다. */
  public Notice updateBy(NoticeUpdateRequest request) {
    this.title = request.getTitle().trim();
    this.content = request.getContent().trim();
    this.category = request.getCategory().trim();
    this.isPinned = request.getIsPinned();
    this.updatedAt = LocalDateTime.now();
    return this;
  }
}

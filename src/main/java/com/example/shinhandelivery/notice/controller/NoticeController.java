package com.example.shinhandelivery.notice.controller;

import com.example.shinhandelivery.notice.dto.request.NoticeCreateRequest;
import com.example.shinhandelivery.notice.dto.request.NoticeUpdateRequest;
import com.example.shinhandelivery.notice.dto.response.NoticeDetailResponse;
import com.example.shinhandelivery.notice.dto.response.NoticeResponse;
import com.example.shinhandelivery.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/** 공지사항 목록 및 상세 조회 API를 제공하는 컨트롤러입니다. */
@Tag(name = "Notice", description = "공지사항 API")
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

  private final NoticeService noticeService;

  /** 공지사항 목록을 페이징 조회합니다. 카테고리 필터링이 가능합니다. */
  @Operation(summary = "공지사항 목록 페이징 조회", description = "상단 고정 및 최신순 공지사항 목록을 페이징으로 조회합니다.")
  @GetMapping
  public ResponseEntity<Page<NoticeResponse>> getNotices(
      @RequestParam(required = false) String category,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<NoticeResponse> responses =
        noticeService.list(category, pageable).map(NoticeResponse::from);
    return ResponseEntity.ok(responses);
  }

  /** 공지사항 단건 상세 정보를 조회합니다. */
  @Operation(summary = "공지사항 상세 조회", description = "ID에 해당하는 공지사항의 상세 본문 정보를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<NoticeDetailResponse> getNoticeDetail(@PathVariable Long id) {
    return ResponseEntity.ok(NoticeDetailResponse.from(noticeService.getById(id)));
  }

  /** 관리자 권한으로 공지사항을 생성합니다. */
  @Operation(summary = "공지사항 생성", description = "관리자가 새로운 공지사항을 생성합니다.")
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<NoticeDetailResponse> createNotice(
      @Valid @RequestBody NoticeCreateRequest request) {
    NoticeDetailResponse response = NoticeDetailResponse.from(noticeService.create(request));
    return ResponseEntity.created(URI.create("/api/v1/notices/" + response.id())).body(response);
  }

  /** 관리자 권한으로 공지사항을 수정합니다. */
  @Operation(summary = "공지사항 수정", description = "관리자가 기존 공지사항의 전체 내용을 수정합니다.")
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<NoticeDetailResponse> updateNotice(
      @PathVariable Long id, @Valid @RequestBody NoticeUpdateRequest request) {
    return ResponseEntity.ok(NoticeDetailResponse.from(noticeService.update(id, request)));
  }

  /** 관리자 권한으로 공지사항을 삭제합니다. */
  @Operation(summary = "공지사항 삭제", description = "관리자가 기존 공지사항을 삭제합니다.")
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
    noticeService.delete(id);
    return ResponseEntity.noContent().build();
  }
}

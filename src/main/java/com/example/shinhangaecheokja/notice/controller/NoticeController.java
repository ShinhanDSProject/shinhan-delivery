package com.example.shinhangaecheokja.notice.controller;

import com.example.shinhangaecheokja.notice.dto.response.NoticeDetailResponse;
import com.example.shinhangaecheokja.notice.dto.response.NoticeResponse;
import com.example.shinhangaecheokja.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        noticeService.getNotices(category, pageable).map(NoticeResponse::from);
    return ResponseEntity.ok(responses);
  }

  /** 공지사항 단건 상세 정보를 조회합니다. */
  @Operation(summary = "공지사항 상세 조회", description = "ID에 해당하는 공지사항의 상세 본문 정보를 조회합니다.")
  @GetMapping("/{id}")
  public ResponseEntity<NoticeDetailResponse> getNoticeDetail(@PathVariable Long id) {
    return ResponseEntity.ok(NoticeDetailResponse.from(noticeService.getNoticeDetail(id)));
  }
}

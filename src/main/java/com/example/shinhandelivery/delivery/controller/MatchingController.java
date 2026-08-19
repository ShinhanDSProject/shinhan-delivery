package com.example.shinhandelivery.delivery.controller;

import com.example.shinhandelivery.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhandelivery.delivery.dto.request.MatchingUpdateRequest;
import com.example.shinhandelivery.delivery.dto.response.DeliveryResponse;
import com.example.shinhandelivery.delivery.dto.response.MatchingResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.service.MatchingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Matching 조회 API를 제공하는 컨트롤러. 조회(GET)는 {@code realtime-tracking.html}처럼 고객이 본인 배송의 매칭 정보를 확인하는 실사용
 * 경로가 있어 로그인한 회원이면 누구나 접근할 수 있다. 반면 생성·변경·삭제는 배송원의 실제 콜 수락({@code
 * DeliveryController#catchDelivery}, 자기 소유 차량으로만 가능)을 우회해 임의의 배송 요청·차량을 직접 매칭시키거나 상태를 바꿀 수 있어 운영자
 * 개입 용도로만 열어둔다.
 */
@RestController
@RequestMapping("/api/v1/matchings")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MatchingController {

  private final MatchingService matchingService;

  /** 매칭을 생성한다. 임의의 배송 요청·차량을 직접 매칭시킬 수 있어 관리자만 호출할 수 있다. */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MatchingResponse> createMatching(
      @RequestBody @Valid MatchingCreateRequest request) {
    Matching created = matchingService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(MatchingResponse.from(created));
  }

  /** 차량이 지금 수락할 수 있는 열린 콜(배송 요청) 목록을 조회한다. */
  @GetMapping("/calls")
  public ResponseEntity<List<DeliveryResponse>> getOpenCalls(@RequestParam Long vehicleId) {
    List<DeliveryRequest> openCalls = matchingService.getOpenCalls(vehicleId);
    return ResponseEntity.ok(openCalls.stream().map(DeliveryResponse::from).toList());
  }

  /** 매칭 단건을 조회한다. */
  @GetMapping("/{matchingId}")
  public ResponseEntity<MatchingResponse> getMatching(@PathVariable Long matchingId) {
    return ResponseEntity.ok(MatchingResponse.from(matchingService.getById(matchingId)));
  }

  /** 매칭 전체 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<MatchingResponse>> getMatchings() {
    List<MatchingResponse> responses =
        matchingService.list().stream().map(MatchingResponse::from).toList();
    return ResponseEntity.ok(responses);
  }

  /** 매칭 상태를 변경한다. 진행 중인 임의의 매칭을 강제로 전이시킬 수 있어 관리자만 호출할 수 있다. */
  @PutMapping("/{matchingId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MatchingResponse> updateMatching(
      @PathVariable Long matchingId, @RequestBody @Valid MatchingUpdateRequest request) {
    Matching updated = matchingService.update(matchingId, request);
    return ResponseEntity.ok(MatchingResponse.from(updated));
  }

  /** 매칭을 삭제한다. 관리자만 호출할 수 있다. */
  @DeleteMapping("/{matchingId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteMatching(@PathVariable Long matchingId) {
    matchingService.delete(matchingId);
    return ResponseEntity.noContent().build();
  }
}

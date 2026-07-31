package com.example.shinhangaecheokja.delivery.controller;

import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.dto.request.MatchingUpdateRequest;
import com.example.shinhangaecheokja.delivery.dto.response.DeliveryResponse;
import com.example.shinhangaecheokja.delivery.dto.response.MatchingResponse;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.Matching;
import com.example.shinhangaecheokja.delivery.service.MatchingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Matching CRUD API를 제공하는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/matchings")
@RequiredArgsConstructor
public class MatchingController {

  private final MatchingService matchingService;

  /** 매칭을 생성한다. */
  @PostMapping
  public ResponseEntity<MatchingResponse> createMatching(
      @RequestBody @Valid MatchingCreateRequest request) {
    Matching created = matchingService.createMatching(request);
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
    return ResponseEntity.ok(matchingService.getMatching(matchingId));
  }

  /** 매칭 전체 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<MatchingResponse>> getMatchings() {
    return ResponseEntity.ok(matchingService.getMatchings());
  }

  /** 매칭 상태를 변경한다. */
  @PutMapping("/{matchingId}")
  public ResponseEntity<MatchingResponse> updateMatching(
      @PathVariable Long matchingId, @RequestBody @Valid MatchingUpdateRequest request) {
    return ResponseEntity.ok(matchingService.updateMatching(matchingId, request));
  }

  /** 매칭을 삭제한다. */
  @DeleteMapping("/{matchingId}")
  public ResponseEntity<Void> deleteMatching(@PathVariable Long matchingId) {
    matchingService.deleteMatching(matchingId);
    return ResponseEntity.noContent().build();
  }
}

package com.example.shinhangaecheokja.address.controller;

import com.example.shinhangaecheokja.address.dto.request.AddressCreateRequest;
import com.example.shinhangaecheokja.address.dto.request.AddressUpdateRequest;
import com.example.shinhangaecheokja.address.dto.response.AddressResponse;
import com.example.shinhangaecheokja.address.service.AddressService;
import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;
import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 자주 쓰는 주소 CRUD API를 제공하는 컨트롤러. */
@RestController
@RequestMapping({"/api/v1/addresses", "/api/addresses"})
@RequiredArgsConstructor
public class AddressController {

  private final AddressService addressService;

  /** 인증된 본인의 자주 쓰는 주소 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<AddressResponse>> getAddresses(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = extractMemberId(userDetails);
    return ResponseEntity.ok(addressService.getAddresses(memberId));
  }

  /** 인증된 본인의 신규 자주 쓰는 주소를 생성한다. */
  @PostMapping
  public ResponseEntity<AddressResponse> createAddress(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody AddressCreateRequest request) {
    Long memberId = extractMemberId(userDetails);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(addressService.createAddress(memberId, request));
  }

  /** 인증된 본인의 자주 쓰는 주소를 수정한다. */
  @PatchMapping("/{id}")
  public ResponseEntity<AddressResponse> updateAddress(
      @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody AddressUpdateRequest request) {
    Long memberId = extractMemberId(userDetails);
    return ResponseEntity.ok(addressService.updateAddress(id, memberId, request));
  }

  /** 인증된 본인의 자주 쓰는 주소를 삭제한다. */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAddress(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = extractMemberId(userDetails);
    addressService.deleteAddress(id, memberId);
    return ResponseEntity.noContent().build();
  }

  private Long extractMemberId(CustomUserDetails userDetails) {
    if (userDetails != null && userDetails.getId() != null) {
      return userDetails.getId();
    }
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null
        && auth.getPrincipal() instanceof CustomUserDetails customUser
        && customUser.getId() != null) {
      return customUser.getId();
    }
    throw new BusinessException(ErrorCode.UNAUTHORIZED);
  }
}

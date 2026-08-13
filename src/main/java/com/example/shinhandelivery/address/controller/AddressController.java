package com.example.shinhandelivery.address.controller;

import com.example.shinhandelivery.address.dto.request.AddressCreateRequest;
import com.example.shinhandelivery.address.dto.request.AddressUpdateRequest;
import com.example.shinhandelivery.address.dto.response.AddressResponse;
import com.example.shinhandelivery.address.entity.Address;
import com.example.shinhandelivery.address.service.AddressService;
import com.example.shinhandelivery.common.exception.BusinessException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  /** 로그인 회원의 자주 쓰는 주소 목록을 조회한다. */
  @GetMapping
  public ResponseEntity<List<AddressResponse>> getAddresses(
      @AuthenticationPrincipal CustomUserDetails customUser) {
    List<AddressResponse> responses =
        addressService.list(extractMemberId(customUser)).stream()
            .map(AddressResponse::from)
            .toList();
    return ResponseEntity.ok(responses);
  }

  /** 신규 자주 쓰는 주소를 생성한다. */
  @PostMapping
  public ResponseEntity<AddressResponse> createAddress(
      @AuthenticationPrincipal CustomUserDetails principal,
      @RequestBody @Valid AddressCreateRequest request) {
    Long memberId = extractMemberId(principal);
    Address created = addressService.create(memberId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(AddressResponse.from(created));
  }

  /** 인증된 본인의 자주 쓰는 주소를 수정한다. */
  @PatchMapping("/{id}")
  public ResponseEntity<AddressResponse> updateAddress(
      @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody AddressUpdateRequest request) {
    Long memberId = extractMemberId(userDetails);
    Address updated = addressService.update(memberId, id, request);
    return ResponseEntity.ok(AddressResponse.from(updated));
  }

  /** 인증된 본인의 자주 쓰는 주소를 삭제한다. */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAddress(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = extractMemberId(userDetails);
    addressService.delete(memberId, id);
    return ResponseEntity.noContent().build();
  }

  private Long extractMemberId(CustomUserDetails userDetails) {
    if (userDetails == null || userDetails.getId() == null) {
      throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }
    return userDetails.getId();
  }
}

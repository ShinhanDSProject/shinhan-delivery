package com.example.shinhangaecheokja.delivery.exception;

import com.example.shinhangaecheokja.common.exception.BusinessException;
import com.example.shinhangaecheokja.common.exception.ErrorCode;

/** 완료되지 않았거나 증거 사진이 등록되지 않은 배송 요청의 사진을 조회할 때 던진다. */
public class ProofPhotoNotFoundException extends BusinessException {

  public ProofPhotoNotFoundException(Long deliveryRequestId) {
    super(ErrorCode.PROOF_PHOTO_NOT_FOUND, "완료 증거 사진을 찾을 수 없습니다: " + deliveryRequestId);
  }
}

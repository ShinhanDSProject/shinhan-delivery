package com.example.shinhangaecheokja.payment.dto.response;

import java.time.LocalDateTime;

/** 포인트 사용 결과 응답 DTO. */
public record PointUseResultResponse(long balance, long usedAmount, LocalDateTime paidAt) {}

package com.example.shinhangaecheokja.payment.dto.response;

import java.time.LocalDateTime;

/** 포인트 충전 결과 응답 DTO. */
public record PointBalanceResponse(long balance, LocalDateTime lastChargedAt) {}

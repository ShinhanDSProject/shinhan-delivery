package com.example.shinhangaecheokja.payment.dto.response;

import java.time.LocalDateTime;

/** 로그인 회원의 포인트 잔액 응답 DTO. */
public record PointBalanceResponse(long balance, LocalDateTime lastChargedAt) {}

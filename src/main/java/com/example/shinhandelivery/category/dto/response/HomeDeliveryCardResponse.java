package com.example.shinhandelivery.category.dto.response;

/** 홈 화면에 표시할 진행 중인 배송 카드 뷰 응답 DTO. */
public record HomeDeliveryCardResponse(Long id, String routeLabel, String etaLabel) {}

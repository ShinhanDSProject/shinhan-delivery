package com.example.shinhandelivery.tracking.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import java.time.LocalDateTime;

/** {@code /topic/delivery/{deliveryId}/status} 구독자에게 브로드캐스트되는 상태 변경 응답 DTO. */
public record DeliveryStatusBroadcastResponse(
    Long deliveryId, DeliveryStatus status, LocalDateTime timestamp) {}

package com.example.shinhandelivery.realtime.dto.response;

import java.time.LocalDateTime;

/** {@code /topic/delivery/{deliveryId}/location} 구독자에게 브로드캐스트되는 위치 응답 DTO. */
public record LocationBroadcastResponse(
    Long deliveryId, Long vehicleId, double latitude, double longitude, LocalDateTime timestamp) {}

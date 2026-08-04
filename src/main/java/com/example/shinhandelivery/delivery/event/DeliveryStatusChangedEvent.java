package com.example.shinhandelivery.delivery.event;

import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import java.time.LocalDateTime;

/**
 * 배송 상태가 전이됐을 때 발행되는 이벤트. delivery 패키지는 이 이벤트를 발행하기만 하고, 실시간 브로드캐스트(WebSocket)는 tracking 패키지의 리스너가
 * 담당한다 — delivery가 tracking(메시징 인프라)에 직접 의존하지 않도록 분리하기 위함이다.
 */
public record DeliveryStatusChangedEvent(
    Long deliveryRequestId, DeliveryStatus status, LocalDateTime timestamp) {}

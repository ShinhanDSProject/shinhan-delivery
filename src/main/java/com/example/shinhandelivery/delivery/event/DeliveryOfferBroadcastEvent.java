package com.example.shinhandelivery.delivery.event;

import com.example.shinhandelivery.delivery.dto.response.DeliveryResponse;
import java.util.List;

/**
 * 신규 배송 요청이 생성됐을 때, 그 요청을 감당할 수 있는 후보 차량들에게 발행되는 이벤트. delivery 패키지는 이 이벤트를 발행하기만 하고, 실시간
 * 브로드캐스트(WebSocket)는 realtime 패키지의 리스너가 담당한다.
 */
public record DeliveryOfferBroadcastEvent(List<Long> candidateVehicleIds, DeliveryResponse offer) {}

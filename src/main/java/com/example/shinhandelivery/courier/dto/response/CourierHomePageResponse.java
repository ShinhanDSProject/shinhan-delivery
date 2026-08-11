package com.example.shinhandelivery.courier.dto.response;

import com.example.shinhandelivery.delivery.dto.response.AvailableDeliveryResponse;
import java.util.List;

/** 배송원 홈 화면(courier-home) SSR 렌더링에 필요한 데이터를 전달하는 응답 DTO. */
public record CourierHomePageResponse(
    String memberName,
    String transportMode,
    String workStatus,
    boolean isOnline,
    double latitude,
    double longitude,
    List<AvailableDeliveryResponse> availableDeliveries,
    long availableCount) {}

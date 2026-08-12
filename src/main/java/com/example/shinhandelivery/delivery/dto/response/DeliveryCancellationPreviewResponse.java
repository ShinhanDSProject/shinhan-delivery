package com.example.shinhandelivery.delivery.dto.response;

import com.example.shinhandelivery.delivery.entity.DeliveryStatus;

/** 현재 배송 상태를 기준으로 계산한 고객 취소 예상 정산 결과. */
public record DeliveryCancellationPreviewResponse(
    Long deliveryRequestId,
    DeliveryStatus currentStatus,
    long paidAmount,
    long cancellationFee,
    long refundAmount,
    long courierCompensation) {}

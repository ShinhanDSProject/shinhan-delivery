package com.example.shinhandelivery.delivery.service;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.common.exception.ErrorCode;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.payment.dto.request.PointRefundRequest;
import com.example.shinhandelivery.payment.service.PaymentService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 30분 동안 배송원이 배정되지 않은 배송 요청을 취소하고 결제 포인트를 환불한다. */
@Service
@RequiredArgsConstructor
public class DeliveryTimeoutService {

  private static final String REFUND_KEY_PREFIX = "delivery-timeout-refund:";
  private static final String REFUND_DESCRIPTION = "30분 미배정 자동 취소 환불";
  private static final long RETRY_BACKOFF_MINUTES = 5;

  private final DeliveryRequestRepository deliveryRequestRepository;
  private final PaymentService paymentService;
  private final ApplicationEventPublisher eventPublisher;

  /** 한 번의 스케줄 실행에서 처리할 만료 후보 ID를 설정된 크기만큼 반환한다. */
  @Transactional(readOnly = true)
  public List<Long> listTimedOutCandidateIds(LocalDateTime cutoff, int batchSize) {
    return deliveryRequestRepository.findTimedOutCandidateIds(
        DeliveryStatus.REQUESTED, cutoff, LocalDateTime.now(), PageRequest.of(0, batchSize));
  }

  /** 실패한 요청의 다음 재시도 시각을 별도 트랜잭션에 기록한다. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void scheduleRetryAfterFailure(Long deliveryRequestId, LocalDateTime failedAt) {
    DeliveryRequest deliveryRequest = findDeliveryRequestForUpdateOrThrow(deliveryRequestId);
    if (deliveryRequest.getStatus() == DeliveryStatus.REQUESTED) {
      deliveryRequest.scheduleTimeoutRetry(failedAt.plusMinutes(RETRY_BACKOFF_MINUTES));
    }
  }

  /** 배송 요청을 잠근 뒤 상태와 생성 시각을 재확인하고, 결제된 요청이면 전액 환불한 후 취소한다. */
  @Transactional
  public boolean expireTimedOutDelivery(
      Long deliveryRequestId, LocalDateTime cutoff, LocalDateTime processedAt) {
    DeliveryRequest deliveryRequest = findDeliveryRequestForUpdateOrThrow(deliveryRequestId);
    if (!isStillTimedOut(deliveryRequest, cutoff)) {
      return false;
    }

    boolean refunded = refundIfPaid(deliveryRequest);
    deliveryRequest.cancelByTimeout(processedAt, refunded);
    eventPublisher.publishEvent(
        new DeliveryStatusChangedEvent(deliveryRequestId, DeliveryStatus.CANCELLED, processedAt));
    return true;
  }

  private boolean refundIfPaid(DeliveryRequest deliveryRequest) {
    if (deliveryRequest.getPaymentIdempotencyKey() == null) {
      return false;
    }
    paymentService.refundPoint(
        deliveryRequest.getMemberId(),
        REFUND_KEY_PREFIX + deliveryRequest.getId(),
        PointRefundRequest.of(
            deliveryRequest.getFeePoint(), deliveryRequest.getId(), REFUND_DESCRIPTION));
    return true;
  }

  private boolean isStillTimedOut(DeliveryRequest deliveryRequest, LocalDateTime cutoff) {
    return deliveryRequest.getStatus() == DeliveryStatus.REQUESTED
        && deliveryRequest.getCreatedAt() != null
        && !deliveryRequest.getCreatedAt().isAfter(cutoff);
  }

  private DeliveryRequest findDeliveryRequestForUpdateOrThrow(Long deliveryRequestId) {
    return deliveryRequestRepository
        .findByIdForUpdate(deliveryRequestId)
        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND));
  }
}

package com.example.shinhandelivery.delivery.scheduler;

import com.example.shinhandelivery.delivery.service.DeliveryTimeoutService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 설정된 주기로 30분 이상 미배정된 배송 요청을 제한된 배치 단위로 처리한다. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "app.delivery-timeout.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class DeliveryTimeoutScheduler {

  private final DeliveryTimeoutService deliveryTimeoutService;

  @Value("${app.delivery-timeout.timeout-minutes:30}")
  private long timeoutMinutes;

  @Value("${app.delivery-timeout.batch-size:100}")
  private int batchSize;

  @Scheduled(fixedDelayString = "${app.delivery-timeout.fixed-delay-ms:60000}")
  public void expireTimedOutDeliveries() {
    LocalDateTime processedAt = LocalDateTime.now();
    LocalDateTime cutoff = processedAt.minusMinutes(timeoutMinutes);
    List<Long> candidateIds = deliveryTimeoutService.listTimedOutCandidateIds(cutoff, batchSize);
    int expiredCount = 0;
    int failedCount = 0;

    for (Long deliveryRequestId : candidateIds) {
      try {
        if (deliveryTimeoutService.expireTimedOutDelivery(deliveryRequestId, cutoff, processedAt)) {
          expiredCount++;
        }
      } catch (RuntimeException exception) {
        failedCount++;
        log.error("배송 자동 타임아웃 처리 실패: deliveryRequestId={}", deliveryRequestId, exception);
        try {
          deliveryTimeoutService.scheduleRetryAfterFailure(deliveryRequestId, processedAt);
        } catch (RuntimeException retryException) {
          log.error(
              "배송 자동 타임아웃 재시도 예약 실패: deliveryRequestId={}", deliveryRequestId, retryException);
        }
      }
    }

    if (!candidateIds.isEmpty()) {
      log.info(
          "배송 자동 타임아웃 배치 완료: candidates={}, expired={}, failed={}",
          candidateIds.size(),
          expiredCount,
          failedCount);
    }
  }
}

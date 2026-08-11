package com.example.shinhandelivery.delivery.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shinhandelivery.delivery.service.DeliveryTimeoutService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DeliveryTimeoutSchedulerTest {

  @Mock private DeliveryTimeoutService deliveryTimeoutService;
  @InjectMocks private DeliveryTimeoutScheduler scheduler;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(scheduler, "timeoutMinutes", 30L);
    ReflectionTestUtils.setField(scheduler, "batchSize", 100);
  }

  @Test
  @DisplayName("한 후보의 실패와 무관하게 다음 만료 후보를 계속 처리한다")
  void schedulerContinuesAfterIndividualFailure() {
    when(deliveryTimeoutService.listTimedOutCandidateIds(any(LocalDateTime.class), eq(100)))
        .thenReturn(List.of(1L, 2L));
    when(deliveryTimeoutService.expireTimedOutDelivery(
            eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
        .thenThrow(new IllegalStateException("refund failed"));

    scheduler.expireTimedOutDeliveries();

    verify(deliveryTimeoutService)
        .expireTimedOutDelivery(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class));
  }
}

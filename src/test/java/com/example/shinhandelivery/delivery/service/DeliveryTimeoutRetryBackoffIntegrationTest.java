package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 재시도 백오프 중인 오래된 실패 요청이 뒤의 정상 타임아웃 후보를 가리지 않는지 실제 DB 조회로 검증한다. */
@SpringBootTest
class DeliveryTimeoutRetryBackoffIntegrationTest {

  @Autowired private DeliveryTimeoutService deliveryTimeoutService;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private MemberRepository memberRepository;

  private Long memberId;
  private Long failedDeliveryId;
  private Long laterDeliveryId;

  @AfterEach
  void cleanUp() {
    if (failedDeliveryId != null) {
      deliveryRequestRepository.deleteById(failedDeliveryId);
    }
    if (laterDeliveryId != null) {
      deliveryRequestRepository.deleteById(laterDeliveryId);
    }
    if (memberId != null) {
      memberRepository.deleteById(memberId);
    }
  }

  @Test
  @DisplayName("백오프 중인 실패 요청을 제외하고 뒤의 만료 요청을 배치에 포함한다")
  void retryBackoffPreventsFailedCandidateFromStarvingLaterCandidate() {
    LocalDateTime now = LocalDateTime.now();
    memberId =
        memberRepository
            .save(
                Member.builder()
                    .email("timeout-backoff-" + System.nanoTime() + "@example.com")
                    .password("password")
                    .name("타임아웃 백오프 테스트")
                    .phoneNumber("010-0000-0000")
                    .role(MemberRole.CUSTOMER)
                    .build())
            .getId();
    failedDeliveryId = saveRequestedDelivery(now.minusHours(2), now.plusMinutes(5));
    laterDeliveryId = saveRequestedDelivery(now.minusHours(1), null);

    assertThat(deliveryTimeoutService.listTimedOutCandidateIds(now.minusMinutes(30), 1))
        .containsExactly(laterDeliveryId);
  }

  private Long saveRequestedDelivery(LocalDateTime createdAt, LocalDateTime nextRetryAt) {
    return deliveryRequestRepository
        .save(
            DeliveryRequest.builder()
                .memberId(memberId)
                .pickupAddress("서울시 강남구")
                .dropoffAddress("서울시 서초구")
                .weight(10)
                .distance(5)
                .status(DeliveryStatus.REQUESTED)
                .feePoint(3000L)
                .createdAt(createdAt)
                .timeoutNextRetryAt(nextRetryAt)
                .build())
        .getId();
  }
}

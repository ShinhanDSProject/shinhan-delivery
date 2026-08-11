package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.delivery.entity.DeliveryCancellationReason;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.payment.entity.PointHistory;
import com.example.shinhandelivery.payment.entity.PointHistoryType;
import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
import com.example.shinhandelivery.payment.repository.PointHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 동일 만료 배송을 여러 서버가 동시에 처리하는 상황을 실제 DB 비관적 락으로 검증한다. */
@SpringBootTest
class DeliveryTimeoutConcurrencyTest {

  private static final int REQUEST_COUNT = 100;
  private static final long REFUND_AMOUNT = 3000L;

  @Autowired private DeliveryTimeoutService deliveryTimeoutService;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private PointHistoryRepository pointHistoryRepository;
  @Autowired private MemberRepository memberRepository;

  private Long memberId;
  private Long walletId;
  private Long deliveryRequestId;
  private Long refundHistoryId;

  @AfterEach
  void cleanUp() {
    if (refundHistoryId != null) {
      pointHistoryRepository.deleteById(refundHistoryId);
    }
    if (deliveryRequestId != null) {
      deliveryRequestRepository.deleteById(deliveryRequestId);
    }
    if (walletId != null) {
      paymentRepository.deleteById(walletId);
    }
    if (memberId != null) {
      memberRepository.deleteById(memberId);
    }
  }

  @Test
  @DisplayName("100개 동시 타임아웃 요청도 배송 취소와 환불은 한 번만 수행한다")
  void concurrentTimeoutRequestsCancelAndRefundOnlyOnce() throws InterruptedException {
    createPaidTimedOutDelivery();
    LocalDateTime processedAt = LocalDateTime.of(2026, 8, 11, 16, 0);
    LocalDateTime cutoff = processedAt.minusMinutes(30);
    ExecutorService executorService = Executors.newFixedThreadPool(REQUEST_COUNT);
    CountDownLatch readyLatch = new CountDownLatch(REQUEST_COUNT);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(REQUEST_COUNT);
    AtomicInteger expiredCount = new AtomicInteger();
    List<Throwable> unexpectedFailures = new CopyOnWriteArrayList<>();

    for (int i = 0; i < REQUEST_COUNT; i++) {
      executorService.submit(
          () -> {
            readyLatch.countDown();
            try {
              startLatch.await();
              if (deliveryTimeoutService.expireTimedOutDelivery(
                  deliveryRequestId, cutoff, processedAt)) {
                expiredCount.incrementAndGet();
              }
            } catch (InterruptedException exception) {
              Thread.currentThread().interrupt();
            } catch (Throwable throwable) {
              unexpectedFailures.add(throwable);
            } finally {
              doneLatch.countDown();
            }
          });
    }

    boolean completedInTime;
    try {
      readyLatch.await();
      startLatch.countDown();
      completedInTime = doneLatch.await(30, TimeUnit.SECONDS);
    } finally {
      executorService.shutdown();
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
      }
    }

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(completedInTime).as("모든 스레드가 제한시간 내 종료됨").isTrue();
    softly.assertThat(unexpectedFailures).as("예상하지 못한 실패 없음").isEmpty();
    softly.assertAll();

    DeliveryRequest deliveryRequest =
        deliveryRequestRepository.findById(deliveryRequestId).orElseThrow();
    PointWallet wallet = paymentRepository.findById(walletId).orElseThrow();
    PointHistory history =
        pointHistoryRepository
            .findByMemberIdAndIdempotencyKey(
                memberId, "delivery-timeout-refund:" + deliveryRequestId)
            .orElseThrow();
    refundHistoryId = history.getId();

    assertThat(expiredCount.get()).isEqualTo(1);
    assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
    assertThat(deliveryRequest.getCancellationReason())
        .isEqualTo(DeliveryCancellationReason.AUTO_TIMEOUT);
    assertThat(wallet.getBalance()).isEqualTo(REFUND_AMOUNT);
    assertThat(history.getType()).isEqualTo(PointHistoryType.REFUND);
    assertThat(history.getAmount()).isEqualTo(REFUND_AMOUNT);
  }

  private void createPaidTimedOutDelivery() {
    Member member =
        Member.builder()
            .email("timeout-concurrency-" + System.nanoTime() + "@example.com")
            .password("password")
            .name("타임아웃 동시성 테스트")
            .phoneNumber("010-0000-0000")
            .role(MemberRole.CUSTOMER)
            .build();
    memberId = memberRepository.save(member).getId();
    walletId =
        paymentRepository
            .save(PointWallet.builder().memberId(memberId).balance(0L).build())
            .getId();

    DeliveryRequest deliveryRequest =
        DeliveryRequest.builder()
            .memberId(memberId)
            .pickupAddress("서울시 강남구")
            .dropoffAddress("서울시 서초구")
            .weight(10)
            .distance(5)
            .status(DeliveryStatus.REQUESTED)
            .feePoint(REFUND_AMOUNT)
            .paymentIdempotencyKey("payment-timeout")
            .createdAt(LocalDateTime.of(2026, 8, 11, 15, 0))
            .build();
    deliveryRequestId = deliveryRequestRepository.save(deliveryRequest).getId();
  }
}

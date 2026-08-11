package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.exception.AlreadyMatchedException;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.payment.entity.PointHistory;
import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
import com.example.shinhandelivery.payment.repository.PointHistoryRepository;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 배송원 수락과 자동 타임아웃이 동시에 실행될 때 하나의 상태만 확정되는지 검증한다. */
@SpringBootTest
class DeliveryTimeoutMatchingConcurrencyTest {

  @Autowired private DeliveryTimeoutService deliveryTimeoutService;
  @Autowired private MatchingService matchingService;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private MatchingRepository matchingRepository;
  @Autowired private VehicleRepository vehicleRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private PointHistoryRepository pointHistoryRepository;
  @Autowired private MemberRepository memberRepository;

  private Long customerId;
  private Long courierId;
  private Long walletId;
  private Long vehicleId;
  private Long deliveryRequestId;
  private Long matchingId;
  private Long refundHistoryId;

  @AfterEach
  void cleanUp() {
    if (matchingId != null) {
      matchingRepository.deleteById(matchingId);
    }
    if (refundHistoryId != null) {
      pointHistoryRepository.deleteById(refundHistoryId);
    }
    if (deliveryRequestId != null) {
      deliveryRequestRepository.deleteById(deliveryRequestId);
    }
    if (vehicleId != null) {
      vehicleRepository.deleteById(vehicleId);
    }
    if (walletId != null) {
      paymentRepository.deleteById(walletId);
    }
    if (courierId != null) {
      memberRepository.deleteById(courierId);
    }
    if (customerId != null) {
      memberRepository.deleteById(customerId);
    }
  }

  @Test
  @DisplayName("콜 수락과 자동 타임아웃이 경합하면 매칭 또는 취소 중 하나만 성공한다")
  void matchingAndTimeoutRaceHasSingleWinner() throws InterruptedException {
    createFixture();
    MatchingCreateRequest matchingRequest = new MatchingCreateRequest();
    matchingRequest.setDeliveryRequestId(deliveryRequestId);
    matchingRequest.setVehicleId(vehicleId);
    LocalDateTime processedAt = LocalDateTime.of(2026, 8, 11, 16, 0);
    LocalDateTime cutoff = processedAt.minusMinutes(30);
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch readyLatch = new CountDownLatch(2);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(2);
    AtomicBoolean matched = new AtomicBoolean();
    AtomicBoolean timedOut = new AtomicBoolean();
    List<Throwable> unexpectedFailures = new CopyOnWriteArrayList<>();

    executorService.submit(
        () -> {
          readyLatch.countDown();
          try {
            startLatch.await();
            Matching matching = matchingService.create(matchingRequest);
            matchingId = matching.getId();
            matched.set(true);
          } catch (AlreadyMatchedException ignored) {
            // 자동 취소가 먼저 배송 행 락을 획득한 정상 경합 결과다.
          } catch (Throwable throwable) {
            unexpectedFailures.add(throwable);
          } finally {
            doneLatch.countDown();
          }
        });
    executorService.submit(
        () -> {
          readyLatch.countDown();
          try {
            startLatch.await();
            timedOut.set(
                deliveryTimeoutService.expireTimedOutDelivery(
                    deliveryRequestId, cutoff, processedAt));
          } catch (Throwable throwable) {
            unexpectedFailures.add(throwable);
          } finally {
            doneLatch.countDown();
          }
        });

    readyLatch.await();
    startLatch.countDown();
    boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);

    DeliveryRequest deliveryRequest =
        deliveryRequestRepository.findById(deliveryRequestId).orElseThrow();
    PointWallet wallet = paymentRepository.findById(walletId).orElseThrow();
    pointHistoryRepository
        .findByMemberIdAndIdempotencyKey(customerId, "delivery-timeout-refund:" + deliveryRequestId)
        .map(PointHistory::getId)
        .ifPresent(id -> refundHistoryId = id);

    assertThat(completed).isTrue();
    assertThat(unexpectedFailures).isEmpty();
    assertThat(matched.get()).isNotEqualTo(timedOut.get());
    if (matched.get()) {
      assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.MATCHED);
      assertThat(wallet.getBalance()).isZero();
    } else {
      assertThat(deliveryRequest.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
      assertThat(wallet.getBalance()).isEqualTo(3000L);
    }
  }

  private void createFixture() {
    customerId = memberRepository.save(member("timeout-customer", MemberRole.CUSTOMER)).getId();
    courierId = memberRepository.save(member("timeout-courier", MemberRole.COURIER)).getId();
    walletId =
        paymentRepository
            .save(PointWallet.builder().memberId(customerId).balance(0L).build())
            .getId();
    vehicleId =
        vehicleRepository
            .save(
                Vehicle.builder()
                    .memberId(courierId)
                    .type(VehicleType.CAR)
                    .status(VehicleStatus.AVAILABLE)
                    .maxWeight(100)
                    .maxDistance(100)
                    .build())
            .getId();
    deliveryRequestId =
        deliveryRequestRepository
            .save(
                DeliveryRequest.builder()
                    .memberId(customerId)
                    .pickupAddress("서울시 강남구")
                    .dropoffAddress("서울시 서초구")
                    .weight(10)
                    .distance(5)
                    .status(DeliveryStatus.REQUESTED)
                    .feePoint(3000L)
                    .paymentIdempotencyKey("payment-race")
                    .createdAt(LocalDateTime.of(2026, 8, 11, 15, 0))
                    .build())
            .getId();
  }

  private Member member(String prefix, MemberRole role) {
    return Member.builder()
        .email(prefix + "-" + System.nanoTime() + "@example.com")
        .password("password")
        .name(prefix)
        .phoneNumber("010-0000-0000")
        .role(role)
        .build();
  }
}

package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.payment.entity.PointHistoryType;
import com.example.shinhandelivery.payment.entity.PointWallet;
import com.example.shinhandelivery.payment.repository.PaymentRepository;
import com.example.shinhandelivery.payment.repository.PointHistoryRepository;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** 동일한 배정 배송에 고객 취소가 동시에 들어와도 환불과 배송원 보상이 한 번만 처리되는지 검증한다. */
@SpringBootTest
class DeliveryCancellationConcurrencyTest {

  private static final int REQUEST_COUNT = 100;

  @Autowired private DeliveryCancellationService cancellationService;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private MatchingRepository matchingRepository;
  @Autowired private VehicleRepository vehicleRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private PointHistoryRepository pointHistoryRepository;
  @Autowired private MemberRepository memberRepository;

  private Long customerId;
  private Long courierId;
  private Long deliveryId;
  private Long vehicleId;

  @AfterEach
  void cleanUp() {
    if (deliveryId != null) {
      matchingRepository.findByDeliveryRequestId(deliveryId).ifPresent(matchingRepository::delete);
      pointHistoryRepository
          .findByTypeAndReferenceId(PointHistoryType.REFUND, deliveryId)
          .ifPresent(pointHistoryRepository::delete);
      pointHistoryRepository
          .findByTypeAndReferenceId(PointHistoryType.COURIER_COMPENSATION, deliveryId)
          .ifPresent(pointHistoryRepository::delete);
      deliveryRequestRepository.deleteById(deliveryId);
    }
    if (vehicleId != null) {
      vehicleRepository.deleteById(vehicleId);
    }
    if (customerId != null) {
      paymentRepository.findByMemberId(customerId).ifPresent(paymentRepository::delete);
    }
    if (courierId != null) {
      paymentRepository.findByMemberId(courierId).ifPresent(paymentRepository::delete);
    }
    if (customerId != null) {
      memberRepository.deleteById(customerId);
    }
    if (courierId != null) {
      memberRepository.deleteById(courierId);
    }
  }

  @Test
  @DisplayName("100개 동시 고객 취소에도 환불과 배송원 보상은 각각 한 번만 반영된다")
  void concurrentCancellationSettlesOnce() throws InterruptedException {
    createFixture();
    ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
    CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(REQUEST_COUNT);
    List<Throwable> failures = new CopyOnWriteArrayList<>();

    for (int i = 0; i < REQUEST_COUNT; i++) {
      executor.submit(
          () -> {
            ready.countDown();
            try {
              start.await();
              cancellationService.cancel(customerId, deliveryId);
            } catch (Throwable throwable) {
              failures.add(throwable);
            } finally {
              done.countDown();
            }
          });
    }

    ready.await();
    start.countDown();
    boolean completed = done.await(30, TimeUnit.SECONDS);
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    assertThat(completed).isTrue();
    assertThat(failures).isEmpty();
    assertThat(deliveryRequestRepository.findById(deliveryId).orElseThrow().getStatus())
        .isEqualTo(DeliveryStatus.CANCELLED);
    assertThat(paymentRepository.findByMemberId(customerId).orElseThrow().getBalance())
        .isEqualTo(2000L);
    assertThat(paymentRepository.findByMemberId(courierId).orElseThrow().getBalance())
        .isEqualTo(1000L);
    assertThat(pointHistoryRepository.findByTypeAndReferenceId(PointHistoryType.REFUND, deliveryId))
        .isPresent();
    assertThat(
            pointHistoryRepository.findByTypeAndReferenceId(
                PointHistoryType.COURIER_COMPENSATION, deliveryId))
        .isPresent();
  }

  private void createFixture() {
    customerId = memberRepository.save(member("cancel-customer", MemberRole.CUSTOMER)).getId();
    courierId = memberRepository.save(member("cancel-courier", MemberRole.COURIER)).getId();
    paymentRepository.save(PointWallet.builder().memberId(customerId).balance(0L).build());
    paymentRepository.save(PointWallet.builder().memberId(courierId).balance(0L).build());
    vehicleId =
        vehicleRepository
            .save(
                Vehicle.builder()
                    .memberId(courierId)
                    .type(VehicleType.CAR)
                    .status(VehicleStatus.BUSY)
                    .maxWeight(100)
                    .maxDistance(100)
                    .build())
            .getId();
    deliveryId =
        deliveryRequestRepository
            .save(
                DeliveryRequest.builder()
                    .memberId(customerId)
                    .pickupAddress("서울시 강남구")
                    .dropoffAddress("서울시 서초구")
                    .weight(10)
                    .distance(5)
                    .status(DeliveryStatus.MATCHED)
                    .feePoint(3000L)
                    .paymentIdempotencyKey("payment-cancel")
                    .build())
            .getId();
    matchingRepository.save(Matching.of(deliveryId, vehicleId));
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

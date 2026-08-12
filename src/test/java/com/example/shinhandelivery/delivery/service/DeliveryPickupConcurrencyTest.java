package com.example.shinhandelivery.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
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

/**
 * 동일 배송 요청에 대한 픽업 완료 처리의 동시성 제어(비관적 락)를 실제 DB 트랜잭션으로 검증하는 테스트입니다.
 *
 * <p>Mockito 단위 테스트는 실제 DB 락 경합을 재현할 수 없으므로, 이 테스트는 {@code @SpringBootTest}로 실제 스프링 빈과 트랜잭션을 사용해 동일
 * 배송원 클라이언트가(혹은 중복 요청이) 같은 배송 요청의 픽업 완료를 동시에 여러 번 호출하는 상황을 재현한다.
 */
@SpringBootTest
class DeliveryPickupConcurrencyTest {

  private static final int REQUEST_COUNT = 50;

  @Autowired private DeliveryService deliveryService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private VehicleRepository vehicleRepository;
  @Autowired private MatchingRepository matchingRepository;

  private Long customerId;
  private Long courierId;
  private Long vehicleId;
  private Long matchingId;
  private Long deliveryRequestId;

  @AfterEach
  void cleanUp() {
    // FK 제약(matching -> delivery_request/vehicle) 때문에 matching을 가장 먼저 지워야 한다.
    if (matchingId != null) {
      matchingRepository.deleteById(matchingId);
    }
    if (deliveryRequestId != null) {
      deliveryRequestRepository.deleteById(deliveryRequestId);
    }
    if (vehicleId != null) {
      vehicleRepository.deleteById(vehicleId);
    }
    if (customerId != null) {
      memberRepository.deleteById(customerId);
    }
    if (courierId != null) {
      memberRepository.deleteById(courierId);
    }
  }

  @Test
  @DisplayName("동시 픽업 완료 요청은 비관적 락으로 직렬화되어 한 건만 성공한다")
  void concurrentPickupConfirmShouldSerializeWithPessimisticLock() throws InterruptedException {
    Member customer = new Member();
    customer.setEmail("pickup-concurrency-test-" + System.nanoTime() + "@example.com");
    customer.setPassword("password");
    customer.setName("픽업 동시성 테스트");
    customer.setPhoneNumber("010-0000-0000");
    customer.setRole(MemberRole.CUSTOMER);
    customerId = memberRepository.save(customer).getId();

    Member courier = new Member();
    courier.setEmail("pickup-concurrency-courier-" + System.nanoTime() + "@example.com");
    courier.setPassword("password");
    courier.setName("픽업 동시성 테스트 배송원");
    courier.setPhoneNumber("010-0000-0001");
    courier.setRole(MemberRole.COURIER);
    courierId = memberRepository.save(courier).getId();

    Vehicle vehicle = Vehicle.createDefault(courierId, VehicleType.MOTORCYCLE, 50.0, 50.0);
    vehicleId = vehicleRepository.save(vehicle).getId();

    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(customerId);
    deliveryRequest.setPickupAddress("서울시 강남구");
    deliveryRequest.setDropoffAddress("서울시 서초구");
    deliveryRequest.setWeight(10);
    deliveryRequest.setDistance(5);
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    deliveryRequest.setFeePoint(600);
    deliveryRequest.setPickupLatitude(37.5);
    deliveryRequest.setPickupLongitude(127.0);
    deliveryRequestId = deliveryRequestRepository.save(deliveryRequest).getId();

    matchingId = matchingRepository.save(Matching.of(deliveryRequestId, vehicleId)).getId();

    ExecutorService executorService = Executors.newFixedThreadPool(REQUEST_COUNT);
    CountDownLatch readyLatch = new CountDownLatch(REQUEST_COUNT);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(REQUEST_COUNT);
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger invalidTransitionCount = new AtomicInteger();
    List<Throwable> unexpectedFailures = new CopyOnWriteArrayList<>();

    for (int i = 0; i < REQUEST_COUNT; i++) {
      executorService.submit(
          () -> {
            readyLatch.countDown();
            try {
              startLatch.await();
              deliveryService.confirmPickup(courierId, deliveryRequestId);
              successCount.incrementAndGet();
            } catch (InvalidDeliveryTransitionException e) {
              invalidTransitionCount.incrementAndGet();
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            } catch (Throwable t) {
              // 락 대기 타임아웃, 커넥션 풀 고갈 등 예상치 못한 실패는 카운터 어디에도 잡히지 않으므로 따로 모아
              // 검증 단계에서 원인을 그대로 드러낸다.
              unexpectedFailures.add(t);
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
      // 위 대기 도중 이 스레드 자체가 인터럽트되어 예외가 던져지더라도, 워커 스레드들이 살아있는 채로
      // @AfterEach의 DB 정리가 실행되지 않도록 종료 처리는 항상 수행한다.
      executorService.shutdown();
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
      }
    }

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(completedInTime).as("모든 스레드가 제한시간 내에 종료됨").isTrue();
    softly.assertThat(unexpectedFailures).as("예상치 못한 예외 없음").isEmpty();
    softly.assertAll();

    DeliveryRequest result = deliveryRequestRepository.findById(deliveryRequestId).orElseThrow();

    assertThat(successCount.get()).isEqualTo(1);
    assertThat(invalidTransitionCount.get()).isEqualTo(REQUEST_COUNT - 1);
    assertThat(result.getStatus()).isEqualTo(DeliveryStatus.PICKED_UP);
  }
}

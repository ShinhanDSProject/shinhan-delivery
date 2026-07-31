package com.example.shinhangaecheokja.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.exception.InvalidDeliveryTransitionException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

  private Long customerId;
  private Long deliveryRequestId;

  @AfterEach
  void cleanUp() {
    if (deliveryRequestId != null) {
      deliveryRequestRepository.deleteById(deliveryRequestId);
    }
    if (customerId != null) {
      memberRepository.deleteById(customerId);
    }
  }

  @Test
  @DisplayName("동일 배송 요청의 픽업 완료를 동시에 여러 번 호출해도 단 1건만 성공한다")
  void 동시_픽업_완료_요청은_비관적_락으로_직렬화되어_한_건만_성공한다() throws InterruptedException {
    Member customer = new Member();
    customer.setEmail("pickup-concurrency-test-" + System.nanoTime() + "@example.com");
    customer.setPassword("password");
    customer.setName("픽업 동시성 테스트");
    customer.setPhoneNumber("010-0000-0000");
    customer.setRole(MemberRole.CUSTOMER);
    customerId = memberRepository.save(customer).getId();

    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setCustomerId(customerId);
    deliveryRequest.setPickupAddress("서울시 강남구");
    deliveryRequest.setDropoffAddress("서울시 서초구");
    deliveryRequest.setWeight(10);
    deliveryRequest.setDistance(5);
    deliveryRequest.setStatus(DeliveryStatus.MATCHED);
    deliveryRequest.setFeePoint(600);
    deliveryRequest.setPickupLatitude(37.5);
    deliveryRequest.setPickupLongitude(127.0);
    deliveryRequestId = deliveryRequestRepository.save(deliveryRequest).getId();

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
              deliveryService.confirmPickup(deliveryRequestId);
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

    readyLatch.await();
    startLatch.countDown();
    boolean completedInTime = doneLatch.await(30, TimeUnit.SECONDS);
    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);

    assertThat(completedInTime).isTrue();
    assertThat(unexpectedFailures).isEmpty();

    DeliveryRequest result = deliveryRequestRepository.findById(deliveryRequestId).orElseThrow();

    assertThat(successCount.get()).isEqualTo(1);
    assertThat(invalidTransitionCount.get()).isEqualTo(REQUEST_COUNT - 1);
    assertThat(result.getStatus()).isEqualTo(DeliveryStatus.PICKED_UP);
  }
}

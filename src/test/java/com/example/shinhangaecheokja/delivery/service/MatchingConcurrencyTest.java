package com.example.shinhangaecheokja.delivery.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhangaecheokja.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.exception.AlreadyMatchedException;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleStatus;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.repository.VehicleRepository;
import java.util.ArrayList;
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
 * 동일 배송 요청에 대한 콜 수락(매칭)의 동시성 제어(비관적 락)를 실제 DB 트랜잭션으로 검증하는 테스트입니다.
 *
 * <p>Mockito 단위 테스트는 실제 DB 락 경합을 재현할 수 없으므로, 이 테스트는 {@code @SpringBootTest}로 실제 스프링 빈과 트랜잭션을 사용해 서로
 * 다른 차량 100대가 동시에 같은 배송 요청을 수락하려는 상황을 재현한다.
 */
@SpringBootTest
class MatchingConcurrencyTest {

  private static final int VEHICLE_COUNT = 100;

  @Autowired private MatchingService matchingService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private VehicleRepository vehicleRepository;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private MatchingRepository matchingRepository;

  private Long customerId;
  private Long courierId;
  private Long deliveryRequestId;
  private final List<Long> vehicleIds = new ArrayList<>();

  @AfterEach
  void cleanUp() {
    matchingRepository.findAll().stream()
        .filter(m -> m.getDeliveryRequestId().equals(deliveryRequestId))
        .forEach(m -> matchingRepository.deleteById(m.getId()));
    vehicleIds.forEach(vehicleRepository::deleteById);
    if (deliveryRequestId != null) {
      deliveryRequestRepository.deleteById(deliveryRequestId);
    }
    if (courierId != null) {
      memberRepository.deleteById(courierId);
    }
    if (customerId != null) {
      memberRepository.deleteById(customerId);
    }
  }

  @Test
  @DisplayName("서로 다른 차량 100대가 동시에 같은 배송 요청을 수락해도 단 1건만 매칭에 성공한다")
  void 동시_콜_수락_요청은_비관적_락으로_직렬화되어_한_건만_성공한다() throws InterruptedException {
    customerId = createMember("customer", MemberRole.CUSTOMER);
    courierId = createMember("courier", MemberRole.COURIER);

    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setCustomerId(customerId);
    deliveryRequest.setPickupAddress("서울시 강남구");
    deliveryRequest.setDropoffAddress("서울시 서초구");
    deliveryRequest.setWeight(10);
    deliveryRequest.setDistance(5);
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    deliveryRequest.setFeePoint(600);
    deliveryRequest.setPickupLatitude(37.5);
    deliveryRequest.setPickupLongitude(127.0);
    deliveryRequestId = deliveryRequestRepository.save(deliveryRequest).getId();

    for (int i = 0; i < VEHICLE_COUNT; i++) {
      Vehicle vehicle = new Vehicle();
      vehicle.setOwnerId(courierId);
      vehicle.setType(VehicleType.CAR);
      vehicle.setMaxWeight(500);
      vehicle.setMaxDistance(500);
      vehicle.setLatitude(37.5);
      vehicle.setLongitude(127.0);
      vehicle.setStatus(VehicleStatus.AVAILABLE);
      vehicleIds.add(vehicleRepository.save(vehicle).getId());
    }

    ExecutorService executorService = Executors.newFixedThreadPool(VEHICLE_COUNT);
    CountDownLatch readyLatch = new CountDownLatch(VEHICLE_COUNT);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch doneLatch = new CountDownLatch(VEHICLE_COUNT);
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger alreadyMatchedCount = new AtomicInteger();
    List<Throwable> unexpectedFailures = new CopyOnWriteArrayList<>();

    for (Long vehicleId : vehicleIds) {
      executorService.submit(
          () -> {
            readyLatch.countDown();
            try {
              startLatch.await();
              MatchingCreateRequest request = new MatchingCreateRequest();
              request.setDeliveryRequestId(deliveryRequestId);
              request.setVehicleId(vehicleId);
              matchingService.createMatching(request);
              successCount.incrementAndGet();
            } catch (AlreadyMatchedException e) {
              alreadyMatchedCount.incrementAndGet();
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

    long matchedCount =
        matchingRepository.findAll().stream()
            .filter(m -> m.getDeliveryRequestId().equals(deliveryRequestId))
            .count();
    long busyVehicleCount =
        vehicleIds.stream()
            .map(id -> vehicleRepository.findById(id).orElseThrow())
            .filter(v -> v.getStatus() == VehicleStatus.BUSY)
            .count();

    assertThat(successCount.get()).isEqualTo(1);
    assertThat(alreadyMatchedCount.get()).isEqualTo(VEHICLE_COUNT - 1);
    assertThat(matchedCount).isEqualTo(1);
    assertThat(busyVehicleCount).isEqualTo(1);
  }

  private Long createMember(String prefix, MemberRole role) {
    Member member = new Member();
    member.setEmail(prefix + "-concurrency-test-" + System.nanoTime() + "@example.com");
    member.setPassword("password");
    member.setName(prefix + " 동시성 테스트");
    member.setPhoneNumber("010-0000-0000");
    member.setRole(role);
    return memberRepository.save(member).getId();
  }
}

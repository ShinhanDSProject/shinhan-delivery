package com.example.shinhandelivery.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.delivery.dto.request.MatchingCreateRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.delivery.service.MatchingService;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.notification.entity.Notification;
import com.example.shinhandelivery.notification.repository.NotificationRepository;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * {@code @TransactionalEventListener(AFTER_COMMIT)} + {@code REQUIRES_NEW} 저장이 실제 스프링 트랜잭션·DB로
 * 동작하는지 검증하는 통합 테스트입니다. Mockito 단위 테스트는 리스너 메서드를 직접 호출해 프록시(트랜잭션 경계)를 우회하므로, 이 테스트는
 * {@code @SpringBootTest}로 실제 서비스 호출을 통해 이벤트가 커밋 후 발행되고, 알림이 진짜 DB에 저장되는지까지 검증한다.
 */
@SpringBootTest
class NotificationCreateListenerIntegrationTest {

  @Autowired private MatchingService matchingService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private VehicleRepository vehicleRepository;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private MatchingRepository matchingRepository;
  @Autowired private NotificationRepository notificationRepository;

  private Long customerId;
  private Long courierId;
  private Long vehicleId;
  private Long deliveryRequestId;

  @AfterEach
  void cleanUp() {
    matchingRepository.findAll().stream()
        .filter(m -> m.getDeliveryRequestId().equals(deliveryRequestId))
        .forEach(m -> matchingRepository.deleteById(m.getId()));
    notificationRepository.findAll().stream()
        .filter(n -> n.getMemberId().equals(customerId) || n.getMemberId().equals(courierId))
        .forEach(n -> notificationRepository.deleteById(n.getId()));
    if (vehicleId != null) {
      vehicleRepository.deleteById(vehicleId);
    }
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
  @DisplayName("실제 매칭 생성 트랜잭션이 커밋되면, 고객과 배송원에게 알림이 진짜로 저장된다")
  void matchingCreateShouldPersistNotificationsForBothPartiesAfterCommit() {
    customerId = createMember("notif-customer", MemberRole.CUSTOMER);
    courierId = createMember("notif-courier", MemberRole.COURIER);

    DeliveryRequest deliveryRequest = new DeliveryRequest();
    deliveryRequest.setMemberId(customerId);
    deliveryRequest.setPickupAddress("Gangnam");
    deliveryRequest.setDropoffAddress("Seocho");
    deliveryRequest.setWeight(10);
    deliveryRequest.setDistance(5);
    deliveryRequest.setStatus(DeliveryStatus.REQUESTED);
    deliveryRequest.setFeePoint(600);
    deliveryRequest.setPickupLatitude(37.5);
    deliveryRequest.setPickupLongitude(127.0);
    deliveryRequestId = deliveryRequestRepository.save(deliveryRequest).getId();

    Vehicle vehicle = new Vehicle();
    vehicle.setMemberId(courierId);
    vehicle.setType(VehicleType.CAR);
    vehicle.setMaxWeight(500);
    vehicle.setMaxDistance(500);
    vehicle.setLatitude(37.5);
    vehicle.setLongitude(127.0);
    vehicle.setStatus(VehicleStatus.AVAILABLE);
    vehicleId = vehicleRepository.save(vehicle).getId();

    MatchingCreateRequest request = new MatchingCreateRequest();
    request.setDeliveryRequestId(deliveryRequestId);
    request.setVehicleId(vehicleId);
    matchingService.create(request);

    List<Notification> customerNotifications =
        notificationRepository.findAll().stream()
            .filter(n -> n.getMemberId().equals(customerId))
            .toList();
    List<Notification> courierNotifications =
        notificationRepository.findAll().stream()
            .filter(n -> n.getMemberId().equals(courierId))
            .toList();

    assertThat(customerNotifications).hasSize(1);
    assertThat(customerNotifications.get(0).getId()).isNotNull();
    assertThat(customerNotifications.get(0).getTitle()).isEqualTo("배송기사 매칭 완료");
    assertThat(customerNotifications.get(0).getCategory()).isEqualTo("DELIVERY");

    assertThat(courierNotifications).hasSize(1);
    assertThat(courierNotifications.get(0).getId()).isNotNull();
    assertThat(courierNotifications.get(0).getTitle()).isEqualTo("새 배송 배정");
  }

  private Long createMember(String prefix, MemberRole role) {
    Member member = new Member();
    member.setEmail(prefix + "-" + System.nanoTime() + "@example.com");
    member.setPassword("password");
    member.setName(prefix);
    member.setPhoneNumber("010-0000-0000");
    member.setRole(role);
    return memberRepository.save(member).getId();
  }
}

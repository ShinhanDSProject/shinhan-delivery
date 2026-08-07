package com.example.shinhandelivery.realtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhandelivery.common.security.JwtProvider;
import com.example.shinhandelivery.delivery.dto.request.DeliveryCreateRequest;
import com.example.shinhandelivery.delivery.dto.response.DeliveryResponse;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.ItemSize;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.entity.MatchingStatus;
import com.example.shinhandelivery.delivery.repository.DeliveryRequestRepository;
import com.example.shinhandelivery.delivery.repository.MatchingRepository;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.member.entity.Member;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.member.repository.MemberRepository;
import com.example.shinhandelivery.realtime.dto.request.LocationUpdateRequest;
import com.example.shinhandelivery.realtime.dto.response.DeliveryStatusBroadcastResponse;
import com.example.shinhandelivery.realtime.dto.response.LocationBroadcastResponse;
import com.example.shinhandelivery.vehicle.entity.Vehicle;
import com.example.shinhandelivery.vehicle.entity.VehicleStatus;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import com.example.shinhandelivery.vehicle.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * WebSocketStompClient로 실제 임베디드 서버에 접속해, 배송원이 발행한 위치와 배송 상태 변경이 매칭된 당사자에게만 브로드캐스트되고 관계없는 회원에게는 전달되지
 * 않는지 검증하는 통합 테스트입니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrackingIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private JwtProvider jwtProvider;
  @Autowired private MemberRepository memberRepository;
  @Autowired private VehicleRepository vehicleRepository;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private MatchingRepository matchingRepository;
  @Autowired private DeliveryService deliveryService;
  @Autowired private SimpUserRegistry userRegistry;

  private WebSocketStompClient stompClient;

  private Long customerId;
  private Long courierId;
  private Long strangerId;
  private Long vehicleId;
  private Long deliveryId;

  @BeforeEach
  void setUp() {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setObjectMapper(new ObjectMapper().registerModule(new JavaTimeModule()));
    stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    stompClient.setMessageConverter(converter);

    customerId = createMember("customer", MemberRole.CUSTOMER);
    courierId = createMember("courier", MemberRole.COURIER);
    strangerId = createMember("stranger", MemberRole.CUSTOMER);

    Vehicle vehicle = new Vehicle();
    vehicle.setMemberId(courierId);
    vehicle.setType(VehicleType.CAR);
    vehicle.setMaxWeight(500);
    vehicle.setMaxDistance(500);
    vehicle.setLatitude(37.5);
    vehicle.setLongitude(127.0);
    vehicle.setStatus(VehicleStatus.BUSY);
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
    deliveryId = deliveryRequestRepository.save(deliveryRequest).getId();

    Matching matching = new Matching();
    matching.setDeliveryRequestId(deliveryId);
    matching.setVehicleId(vehicleId);
    matching.setStatus(MatchingStatus.MATCHED);
    matching.setMatchedAt(LocalDateTime.now());
    matchingRepository.save(matching);
  }

  @AfterEach
  void cleanUp() {
    matchingRepository.findAll().stream()
        .filter(m -> m.getDeliveryRequestId().equals(deliveryId))
        .forEach(m -> matchingRepository.deleteById(m.getId()));
    deliveryRequestRepository.deleteById(deliveryId);
    vehicleRepository.deleteById(vehicleId);
    memberRepository.deleteById(courierId);
    memberRepository.deleteById(customerId);
    memberRepository.deleteById(strangerId);
  }

  @Test
  @DisplayName("배송원 위치 발행시 고객이 브로드캐스트를 수신한다")
  void broadcastLocationSuccessShouldReceiveBroadcast() throws Exception {
    BlockingQueue<LocationBroadcastResponse> received = subscribeAsCustomer();

    StompSession courierSession = connect(courierId, "COURIER");
    courierSession.send(
        "/app/delivery/" + deliveryId + "/location", locationUpdate(37.501, 127.001));

    LocationBroadcastResponse broadcast = received.poll(5, TimeUnit.SECONDS);

    assertThat(broadcast).isNotNull();
    assertThat(broadcast.deliveryId()).isEqualTo(deliveryId);
    assertThat(broadcast.vehicleId()).isEqualTo(vehicleId);
    assertThat(broadcast.latitude()).isEqualTo(37.501);
    assertThat(broadcast.longitude()).isEqualTo(127.001);
  }

  @Test
  @DisplayName("권한없는 회원의 구독과 발행은 무시된다")
  void broadcastLocationUnauthorizedShouldBeIgnored() throws Exception {
    BlockingQueue<LocationBroadcastResponse> customerReceived = subscribeAsCustomer();

    BlockingQueue<LocationBroadcastResponse> strangerReceived = new LinkedBlockingQueue<>();
    StompSession strangerSubscribeSession = connect(strangerId, "CUSTOMER");
    strangerSubscribeSession.subscribe(
        "/topic/delivery/" + deliveryId + "/location",
        frameHandler(strangerReceived, LocationBroadcastResponse.class));

    StompSession strangerSendSession = connect(strangerId, "CUSTOMER");
    strangerSendSession.send("/app/delivery/" + deliveryId + "/location", locationUpdate(0.0, 0.0));

    assertThat(customerReceived.poll(1, TimeUnit.SECONDS)).isNull();

    StompSession courierSession = connect(courierId, "COURIER");
    courierSession.send(
        "/app/delivery/" + deliveryId + "/location", locationUpdate(37.501, 127.001));

    assertThat(customerReceived.poll(5, TimeUnit.SECONDS)).isNotNull();
    assertThat(strangerReceived.poll(1, TimeUnit.SECONDS)).isNull();
  }

  @Test
  @DisplayName("배송 상태가 바뀌면 당사자(고객)가 상태 변경 브로드캐스트를 수신한다")
  void broadcastStatusChangeShouldReachOwner() throws Exception {
    BlockingQueue<DeliveryStatusBroadcastResponse> received = subscribeToStatusAsCustomer();

    deliveryService.confirmPickup(deliveryId);

    DeliveryStatusBroadcastResponse broadcast = received.poll(5, TimeUnit.SECONDS);

    assertThat(broadcast).isNotNull();
    assertThat(broadcast.deliveryId()).isEqualTo(deliveryId);
    assertThat(broadcast.status()).isEqualTo(DeliveryStatus.PICKED_UP);
  }

  @Test
  @DisplayName("권한없는 회원은 상태 변경 채널을 구독해도 브로드캐스트를 받지 못한다")
  void broadcastStatusChangeUnauthorizedShouldBeIgnored() throws Exception {
    BlockingQueue<DeliveryStatusBroadcastResponse> strangerReceived = new LinkedBlockingQueue<>();
    StompSession strangerSession = connect(strangerId, "CUSTOMER");
    strangerSession.subscribe(
        "/topic/delivery/" + deliveryId + "/status",
        frameHandler(strangerReceived, DeliveryStatusBroadcastResponse.class));

    deliveryService.confirmPickup(deliveryId);

    assertThat(strangerReceived.poll(2, TimeUnit.SECONDS)).isNull();
  }

  @Test
  @DisplayName("차량 소유주가 오퍼 채널을 구독하면 조건에 맞는 신규 배송요청 오퍼를 수신한다")
  void subscribeOfferAsVehicleOwnerShouldReceiveBroadcast() throws Exception {
    Long offerCourierId = createMember("offer-courier", MemberRole.COURIER);
    Long offerVehicleId = createAvailableVehicle(offerCourierId);
    try {
      BlockingQueue<DeliveryResponse> received = new LinkedBlockingQueue<>();
      StompSession session = connect(offerCourierId, "COURIER");
      String destination = "/topic/vehicles/" + offerVehicleId + "/offers";
      session.subscribe(destination, frameHandler(received, DeliveryResponse.class));
      awaitSubscriptionRegistered(offerCourierId, destination);

      DeliveryRequest created = deliveryService.requestDelivery(customerId, newDeliveryRequest());
      try {
        DeliveryResponse offer = received.poll(5, TimeUnit.SECONDS);

        assertThat(offer).isNotNull();
        assertThat(offer.id()).isEqualTo(created.getId());
      } finally {
        deliveryRequestRepository.deleteById(created.getId());
      }
    } finally {
      vehicleRepository.deleteById(offerVehicleId);
      memberRepository.deleteById(offerCourierId);
    }
  }

  @Test
  @DisplayName("차량 소유주가 아니면 오퍼 채널 구독이 거부되어 브로드캐스트를 받지 못한다")
  void subscribeOfferAsNonOwnerShouldBeIgnored() throws Exception {
    Long offerCourierId = createMember("offer-courier", MemberRole.COURIER);
    Long offerVehicleId = createAvailableVehicle(offerCourierId);
    try {
      BlockingQueue<DeliveryResponse> strangerReceived = new LinkedBlockingQueue<>();
      BlockingQueue<Object> subscribeRejections = new LinkedBlockingQueue<>();
      StompSession strangerSession =
          connectWithRejectionCapture(strangerId, "CUSTOMER", subscribeRejections);
      strangerSession.subscribe(
          "/topic/vehicles/" + offerVehicleId + "/offers",
          frameHandler(strangerReceived, DeliveryResponse.class));

      assertThat(subscribeRejections.poll(5, TimeUnit.SECONDS)).isNotNull();

      DeliveryRequest created = deliveryService.requestDelivery(customerId, newDeliveryRequest());
      try {
        assertThat(strangerReceived.poll(2, TimeUnit.SECONDS)).isNull();
      } finally {
        deliveryRequestRepository.deleteById(created.getId());
      }
    } finally {
      vehicleRepository.deleteById(offerVehicleId);
      memberRepository.deleteById(offerCourierId);
    }
  }

  private Long createAvailableVehicle(Long memberId) {
    Vehicle vehicle = new Vehicle();
    vehicle.setMemberId(memberId);
    vehicle.setType(VehicleType.CAR);
    vehicle.setMaxWeight(500);
    vehicle.setMaxDistance(500);
    vehicle.setLatitude(37.5);
    vehicle.setLongitude(127.0);
    vehicle.setStatus(VehicleStatus.AVAILABLE);
    return vehicleRepository.save(vehicle).getId();
  }

  private DeliveryCreateRequest newDeliveryRequest() {
    DeliveryCreateRequest request = new DeliveryCreateRequest();
    request.setPickupAddress("서울시 강남구");
    request.setDropoffAddress("서울시 서초구");
    request.setWeight(10);
    request.setPickupLatitude(37.0);
    request.setPickupLongitude(127.0);
    request.setDropoffLatitude(38.0);
    request.setDropoffLongitude(127.0);
    request.setItemSize(ItemSize.MEDIUM);
    return request;
  }

  private BlockingQueue<LocationBroadcastResponse> subscribeAsCustomer() throws Exception {
    BlockingQueue<LocationBroadcastResponse> received = new LinkedBlockingQueue<>();
    StompSession customerSession = connect(customerId, "CUSTOMER");
    customerSession.subscribe(
        "/topic/delivery/" + deliveryId + "/location",
        frameHandler(received, LocationBroadcastResponse.class));
    return received;
  }

  /**
   * 구독(SUBSCRIBE)은 비동기라 서버가 실제로 등록하기 전에 리턴될 수 있다. 인프로세스로 직접 호출하는 {@code confirmPickup}은 네트워크를 안 타
   * 거의 즉시 실행되므로, {@link SimpUserRegistry}로 브로커에 구독이 실제로 등록됐는지 폴링해 확인한 뒤에야 반환해 두 호출 사이의 경쟁 상태(타이밍에
   * 따라 브로드캐스트를 놓치는 flaky 테스트)를 없앤다. (STOMP RECEIPT 프레임으로 동기화하는 방법도 시도했지만, 이 환경에서 신뢰성 있게 동작하지 않아 같은
   * JVM 안의 실제 브로커 상태를 직접 폴링하는 방식을 택했다.)
   */
  private BlockingQueue<DeliveryStatusBroadcastResponse> subscribeToStatusAsCustomer()
      throws Exception {
    BlockingQueue<DeliveryStatusBroadcastResponse> received = new LinkedBlockingQueue<>();
    StompSession customerSession = connect(customerId, "CUSTOMER");
    String destination = "/topic/delivery/" + deliveryId + "/status";
    customerSession.subscribe(
        destination, frameHandler(received, DeliveryStatusBroadcastResponse.class));
    awaitSubscriptionRegistered(customerId, destination);
    return received;
  }

  /** SimpUserRegistry를 폴링해 브로커에 구독이 실제로 등록될 때까지(최대 5초) 기다린다. */
  private void awaitSubscriptionRegistered(Long memberId, String destination)
      throws InterruptedException {
    String username = memberId + "@test.com";
    long deadline = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < deadline) {
      SimpUser user = userRegistry.getUser(username);
      boolean registered =
          user != null
              && user.getSessions().stream()
                  .flatMap(session -> session.getSubscriptions().stream())
                  .anyMatch(subscription -> destination.equals(subscription.getDestination()));
      if (registered) {
        return;
      }
      Thread.sleep(20);
    }
    throw new AssertionError("구독이 " + destination + "에 등록되지 않았습니다 (타임아웃)");
  }

  private <T> StompFrameHandler frameHandler(BlockingQueue<T> queue, Class<T> payloadType) {
    return new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return payloadType;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        queue.add(payloadType.cast(payload));
      }
    };
  }

  private LocationUpdateRequest locationUpdate(double latitude, double longitude) {
    LocationUpdateRequest request = new LocationUpdateRequest();
    request.setDeliveryId(deliveryId);
    request.setLatitude(latitude);
    request.setLongitude(longitude);
    request.setTimestamp(LocalDateTime.now());
    return request;
  }

  private StompSession connect(Long memberId, String role) throws Exception {
    String token = jwtProvider.createAccessToken(memberId, memberId + "@test.com", role);
    StompHeaders connectHeaders = new StompHeaders();
    connectHeaders.add("Authorization", "Bearer " + token);
    return stompClient
        .connectAsync(
            "ws://localhost:" + port + "/ws",
            (WebSocketHttpHeaders) null,
            connectHeaders,
            new StompSessionHandlerAdapter() {})
        .get(5, TimeUnit.SECONDS);
  }

  /**
   * 거절될 구독을 테스트하기 위해, 세션 레벨 프레임(구독이 특정되지 않은 ERROR 프레임 등)과 세션 예외를 모두 큐에 담는 핸들러로 연결한다. 구독 거절 처리가 서버에서
   * 실제로 끝난 뒤에야 후속 동작(예: 배송요청 생성)을 진행해, 두 호출 사이의 경쟁 상태로 인한 flaky 테스트를 없앤다.
   */
  private StompSession connectWithRejectionCapture(
      Long memberId, String role, BlockingQueue<Object> signals) throws Exception {
    String token = jwtProvider.createAccessToken(memberId, memberId + "@test.com", role);
    StompHeaders connectHeaders = new StompHeaders();
    connectHeaders.add("Authorization", "Bearer " + token);
    return stompClient
        .connectAsync(
            "ws://localhost:" + port + "/ws",
            (WebSocketHttpHeaders) null,
            connectHeaders,
            new StompSessionHandlerAdapter() {
              @Override
              public void handleFrame(StompHeaders headers, Object payload) {
                signals.add(headers);
              }

              @Override
              public void handleException(
                  StompSession session,
                  StompCommand command,
                  StompHeaders headers,
                  byte[] payload,
                  Throwable exception) {
                signals.add(exception);
              }
            })
        .get(5, TimeUnit.SECONDS);
  }

  private Long createMember(String prefix, MemberRole role) {
    Member member = new Member();
    member.setEmail(prefix + "-tracking-test-" + System.nanoTime() + "@example.com");
    member.setPassword("password");
    member.setName(prefix + " 추적 테스트");
    member.setPhoneNumber("010-0000-0000");
    member.setRole(role);
    return memberRepository.save(member).getId();
  }
}

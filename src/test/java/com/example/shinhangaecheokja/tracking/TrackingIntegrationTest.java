package com.example.shinhangaecheokja.tracking;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shinhangaecheokja.common.security.JwtProvider;
import com.example.shinhangaecheokja.delivery.entity.DeliveryRequest;
import com.example.shinhangaecheokja.delivery.entity.DeliveryStatus;
import com.example.shinhangaecheokja.delivery.entity.Matching;
import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;
import com.example.shinhangaecheokja.delivery.repository.DeliveryRequestRepository;
import com.example.shinhangaecheokja.delivery.repository.MatchingRepository;
import com.example.shinhangaecheokja.member.entity.Member;
import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.member.repository.MemberRepository;
import com.example.shinhangaecheokja.tracking.dto.request.LocationUpdateRequest;
import com.example.shinhangaecheokja.tracking.dto.response.LocationBroadcastResponse;
import com.example.shinhangaecheokja.vehicle.entity.Vehicle;
import com.example.shinhangaecheokja.vehicle.entity.VehicleStatus;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import com.example.shinhangaecheokja.vehicle.repository.VehicleRepository;
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
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * WebSocketStompClient로 실제 임베디드 서버에 접속해, 배송원이 발행한 위치가 매칭된 고객에게만 브로드캐스트되고 관계없는 회원에게는 전달되지 않는지 검증하는
 * 통합 테스트입니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrackingIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private JwtProvider jwtProvider;
  @Autowired private MemberRepository memberRepository;
  @Autowired private VehicleRepository vehicleRepository;
  @Autowired private DeliveryRequestRepository deliveryRequestRepository;
  @Autowired private MatchingRepository matchingRepository;

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
    vehicle.setOwnerId(courierId);
    vehicle.setType(VehicleType.CAR);
    vehicle.setMaxWeight(500);
    vehicle.setMaxDistance(500);
    vehicle.setLatitude(37.5);
    vehicle.setLongitude(127.0);
    vehicle.setStatus(VehicleStatus.BUSY);
    vehicleId = vehicleRepository.save(vehicle).getId();

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
        "/topic/delivery/" + deliveryId + "/location", frameHandler(strangerReceived));

    StompSession strangerSendSession = connect(strangerId, "CUSTOMER");
    strangerSendSession.send("/app/delivery/" + deliveryId + "/location", locationUpdate(0.0, 0.0));

    assertThat(customerReceived.poll(1, TimeUnit.SECONDS)).isNull();

    StompSession courierSession = connect(courierId, "COURIER");
    courierSession.send(
        "/app/delivery/" + deliveryId + "/location", locationUpdate(37.501, 127.001));

    assertThat(customerReceived.poll(5, TimeUnit.SECONDS)).isNotNull();
    assertThat(strangerReceived.poll(1, TimeUnit.SECONDS)).isNull();
  }

  private BlockingQueue<LocationBroadcastResponse> subscribeAsCustomer() throws Exception {
    BlockingQueue<LocationBroadcastResponse> received = new LinkedBlockingQueue<>();
    StompSession customerSession = connect(customerId, "CUSTOMER");
    customerSession.subscribe(
        "/topic/delivery/" + deliveryId + "/location", frameHandler(received));
    return received;
  }

  private StompFrameHandler frameHandler(BlockingQueue<LocationBroadcastResponse> queue) {
    return new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return LocationBroadcastResponse.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        queue.add((LocationBroadcastResponse) payload);
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

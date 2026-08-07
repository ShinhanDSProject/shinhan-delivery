package com.example.shinhandelivery.notification.service;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.delivery.entity.DeliveryRequest;
import com.example.shinhandelivery.delivery.entity.DeliveryStatus;
import com.example.shinhandelivery.delivery.entity.Matching;
import com.example.shinhandelivery.delivery.event.DeliveryStatusChangedEvent;
import com.example.shinhandelivery.delivery.service.DeliveryService;
import com.example.shinhandelivery.delivery.service.MatchingService;
import com.example.shinhandelivery.notification.dto.response.NotificationResponse;
import com.example.shinhandelivery.notification.entity.Notification;
import com.example.shinhandelivery.notification.repository.NotificationRepository;
import com.example.shinhandelivery.vehicle.service.VehicleService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 배송 상태 변경 이벤트를 받아 관련 회원(고객·배송원)에게 실제 알림을 생성하고, 그 회원 전용 WebSocket 채널로 실시간 브로드캐스트한다. 트랜잭션이 커밋된 이후에만
 * 반응해, 전이가 롤백되면 알림도 생성되지 않도록 한다.
 */
@Component
@RequiredArgsConstructor
public class NotificationCreateListener {

  private static final String CATEGORY_DELIVERY = "DELIVERY";

  private final DeliveryService deliveryService;
  private final MatchingService matchingService;
  private final VehicleService vehicleService;
  private final NotificationRepository notificationRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
    Copy customerCopy = customerCopyOf(event.status());
    if (customerCopy == null) {
      return;
    }

    DeliveryRequest deliveryRequest = deliveryService.getDeliveryRequest(event.deliveryRequestId());
    notify(deliveryRequest.getMemberId(), customerCopy);

    Copy courierCopy = courierCopyOf(event.status());
    if (courierCopy != null) {
      findCourierId(event.deliveryRequestId())
          .ifPresent(courierId -> notify(courierId, courierCopy));
    }
  }

  /** 배송 요청에 매칭된 차량 소유주(배송원) memberId를 찾는다. 아직 매칭이 없으면 빈 값을 반환한다. */
  private Optional<Long> findCourierId(Long deliveryRequestId) {
    try {
      Matching matching = matchingService.getMatchingByDeliveryRequestId(deliveryRequestId);
      return Optional.of(vehicleService.getById(matching.getVehicleId()).getMemberId());
    } catch (EntityNotFoundException e) {
      return Optional.empty();
    }
  }

  private void notify(Long memberId, Copy copy) {
    Notification saved =
        notificationRepository.save(
            Notification.of(memberId, copy.title(), copy.message(), CATEGORY_DELIVERY, false));
    messagingTemplate.convertAndSend(
        "/topic/members/" + memberId + "/notifications", NotificationResponse.from(saved));
  }

  /** 상태별 고객용 알림 문구. 이 리스너가 다루지 않는 상태(예: REQUESTED)면 null. */
  private Copy customerCopyOf(DeliveryStatus status) {
    return switch (status) {
      case MATCHED -> new Copy("배송기사 매칭 완료", "배송기사가 배정되어 곧 픽업을 시작합니다.");
      case PICKED_UP -> new Copy("픽업 완료", "배송기사가 물품을 픽업했습니다.");
      case COMPLETED -> new Copy("배송 완료", "배송이 완료되었습니다. 이용해주셔서 감사합니다.");
      case CANCELLED -> new Copy("배송 취소", "배송 요청이 취소되었습니다.");
      default -> null;
    };
  }

  /** 상태별 배송원용 알림 문구. PICKED_UP은 본인이 방금 한 행동이라 알림을 보내지 않는다(null). */
  private Copy courierCopyOf(DeliveryStatus status) {
    return switch (status) {
      case MATCHED -> new Copy("새 배송 배정", "배송 요청이 배정되었습니다. 픽업을 진행해주세요.");
      case COMPLETED -> new Copy("배송 완료 처리됨", "배송을 완료 처리했습니다.");
      case CANCELLED -> new Copy("배정 취소", "배정된 배송이 취소되었습니다.");
      default -> null;
    };
  }

  private record Copy(String title, String message) {}
}

package com.example.shinhandelivery.notification.service;

import com.example.shinhandelivery.common.exception.EntityNotFoundException;
import com.example.shinhandelivery.delivery.entity.DeliveryCancellationReason;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

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
  private final PlatformTransactionManager transactionManager;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
    DeliveryRequest deliveryRequest = deliveryService.getDeliveryRequest(event.deliveryRequestId());
    DeliveryNotificationTemplate template =
        DeliveryNotificationTemplate.forDelivery(deliveryRequest, event.status());
    if (template == null) {
      return;
    }
    notify(deliveryRequest.getMemberId(), template.customerTitle(), template.customerMessage());

    if (template.hasCourierCopy()) {
      findCourierId(event.deliveryRequestId())
          .ifPresent(
              courierId -> notify(courierId, template.courierTitle(), template.courierMessage()));
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

  /**
   * 알림을 별도 트랜잭션(REQUIRES_NEW)에 저장해 실제로 커밋을 마친 뒤에만 WebSocket push를 내보낸다. 저장과 발송을 하나의 트랜잭션으로 묶으면, 커밋
   * 전에 이미 나간 메시지가 이후 롤백으로 DB에는 존재하지 않는 알림을 가리키게 될 수 있다.
   */
  private void notify(Long memberId, String title, String message) {
    Notification saved = saveInNewTransaction(memberId, title, message);
    messagingTemplate.convertAndSend(
        "/topic/members/" + memberId + "/notifications", NotificationResponse.from(saved));
  }

  private Notification saveInNewTransaction(Long memberId, String title, String message) {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    return transactionTemplate.execute(
        status ->
            notificationRepository.save(
                Notification.of(memberId, title, message, CATEGORY_DELIVERY, false)));
  }

  /**
   * 배송 상태별 알림 문구를 한곳에 모아 관리한다. 고객/배송원 문구를 각각 별도 switch로 흩어두지 않고, 상태 하나당 이 enum 상수 하나로 대응시켜 문구 추가·수정
   * 시 여기 한 곳만 보면 되게 한다. 이 리스너가 다루지 않는 상태(예: REQUESTED)는 상수가 없다.
   */
  private enum DeliveryNotificationTemplate {
    MATCHED("배송기사 매칭 완료", "배송기사가 배정되어 곧 픽업을 시작합니다.", "새 배송 배정", "배송 요청이 배정되었습니다. 픽업을 진행해주세요."),
    PICKED_UP("픽업 완료", "배송기사가 물품을 픽업했습니다.", null, null),
    COMPLETED("배송 완료", "배송이 완료되었습니다. 이용해주셔서 감사합니다.", "배송 완료 처리됨", "배송을 완료 처리했습니다."),
    CANCELLED("배송 취소", "배송 요청이 취소되었습니다.", "배정 취소", "배정된 배송이 취소되었습니다."),
    AUTO_TIMEOUT_CANCELLED(
        "배송 자동 취소 및 환불 완료", "30분 동안 배송원이 배정되지 않아 배송이 취소되고 결제 포인트가 환불되었습니다.", null, null);

    private final String customerTitle;
    private final String customerMessage;
    private final String courierTitle;
    private final String courierMessage;

    DeliveryNotificationTemplate(
        String customerTitle, String customerMessage, String courierTitle, String courierMessage) {
      this.customerTitle = customerTitle;
      this.customerMessage = customerMessage;
      this.courierTitle = courierTitle;
      this.courierMessage = courierMessage;
    }

    /** 이 리스너가 다루지 않는 상태(예: REQUESTED)면 null. */
    private static DeliveryNotificationTemplate forDelivery(
        DeliveryRequest deliveryRequest, DeliveryStatus status) {
      if (status == DeliveryStatus.CANCELLED
          && deliveryRequest.getCancellationReason() == DeliveryCancellationReason.AUTO_TIMEOUT) {
        return AUTO_TIMEOUT_CANCELLED;
      }
      return switch (status) {
        case MATCHED -> MATCHED;
        case PICKED_UP -> PICKED_UP;
        case COMPLETED -> COMPLETED;
        case CANCELLED -> CANCELLED;
        default -> null;
      };
    }

    /** PICKED_UP은 배송원 본인이 방금 한 행동이라 배송원용 문구가 없다(false). */
    private boolean hasCourierCopy() {
      return courierTitle != null;
    }

    private String customerTitle() {
      return customerTitle;
    }

    private String customerMessage() {
      return customerMessage;
    }

    private String courierTitle() {
      return courierTitle;
    }

    private String courierMessage() {
      return courierMessage;
    }
  }
}

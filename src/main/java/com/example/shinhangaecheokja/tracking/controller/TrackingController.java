package com.example.shinhangaecheokja.tracking.controller;

import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.tracking.dto.request.LocationUpdateRequest;
import com.example.shinhangaecheokja.tracking.exception.UnauthorizedTrackingAccessException;
import com.example.shinhangaecheokja.tracking.service.TrackingService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/** 배송원 클라이언트가 STOMP로 발행하는 실시간 위치를 받아 구독자에게 브로드캐스트하는 컨트롤러. */
@Controller
@RequiredArgsConstructor
public class TrackingController {

  private static final Logger log = LoggerFactory.getLogger(TrackingController.class);

  private final TrackingService trackingService;

  /** 배송원이 보낸 위치를 검증 후 브로드캐스트한다. 응답은 반환값이 아니라 /topic 구독자에게 별도로 전달된다. */
  @MessageMapping("/delivery/{deliveryId}/location")
  public void updateLocation(
      @DestinationVariable Long deliveryId,
      @Valid LocationUpdateRequest request,
      Principal principal) {
    trackingService.broadcastLocation(extractMemberId(principal), deliveryId, request);
  }

  /** GlobalExceptionHandler는 STOMP를 못 잡으므로, 여기서 발행자 본인에게만 에러를 알린다. */
  @MessageExceptionHandler(UnauthorizedTrackingAccessException.class)
  @SendToUser("/queue/errors")
  public String handleUnauthorizedTrackingAccess(UnauthorizedTrackingAccessException e) {
    log.warn("UnauthorizedTrackingAccessException occurred: {}", e.getMessage());
    return e.getMessage();
  }

  private Long extractMemberId(Principal principal) {
    if (principal instanceof Authentication authentication
        && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails.getId();
    }
    throw new AccessDeniedException("인증 정보가 없습니다.");
  }
}

package com.example.shinhangaecheokja.tracking.config;

import com.example.shinhangaecheokja.common.security.CustomUserDetails;
import com.example.shinhangaecheokja.common.security.JwtProvider;
import com.example.shinhangaecheokja.tracking.service.TrackingService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

/** STOMP CONNECT 시 JWT를 검증해 인증 주체를 세션에 설정하고, SUBSCRIBE 시 배송 추적 채널 접근 권한을 검증한다. */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

  private static final String SUBSCRIBE_DESTINATION_PATTERN =
      "/topic/delivery/{deliveryId}/location";
  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  private final JwtProvider jwtProvider;
  private final TrackingService trackingService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null) {
      return message;
    }

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      authenticate(accessor);
    } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      authorizeSubscribe(accessor);
    }
    return message;
  }

  /** CONNECT 시 Authorization 헤더의 JWT를 검증하고, 통과하면 인증 주체를 세션에 고정한다. */
  private void authenticate(StompHeaderAccessor accessor) {
    String bearerToken = accessor.getFirstNativeHeader("Authorization");
    String token =
        (bearerToken != null && bearerToken.startsWith("Bearer "))
            ? bearerToken.substring(7)
            : null;
    if (token == null || !jwtProvider.validateToken(token)) {
      throw new AccessDeniedException("유효하지 않은 인증 토큰입니다.");
    }
    accessor.setUser(jwtProvider.getAuthentication(token));
  }

  /** 배송 추적 채널 구독 시 deliveryId를 뽑아 TrackingService에 권한 검증을 위임한다. */
  private void authorizeSubscribe(StompHeaderAccessor accessor) {
    String destination = accessor.getDestination();
    if (destination == null || !PATH_MATCHER.match(SUBSCRIBE_DESTINATION_PATTERN, destination)) {
      return;
    }
    Long deliveryId =
        Long.valueOf(
            PATH_MATCHER
                .extractUriTemplateVariables(SUBSCRIBE_DESTINATION_PATTERN, destination)
                .get("deliveryId"));
    trackingService.assertCanSubscribe(deliveryId, extractMemberId(accessor.getUser()));
  }

  private Long extractMemberId(Principal principal) {
    if (principal instanceof Authentication authentication
        && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
      return userDetails.getId();
    }
    throw new AccessDeniedException("인증 정보가 없습니다.");
  }
}

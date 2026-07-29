package com.example.shinhangaecheokja.tracking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/** 실시간 배송 위치 추적을 위한 STOMP WebSocket 설정. */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

  /**
   * 인터셉터가 (SimpMessagingTemplate을 쓰는) TrackingService를 참조하고, SimpMessagingTemplate은 이 설정 클래스가 다시
   * 필요하므로 순환 빈 생성을 막기 위해 {@code @Lazy}로 늦게 주입한다.
   */
  public WebSocketConfig(@Lazy StompAuthChannelInterceptor stompAuthChannelInterceptor) {
    this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompAuthChannelInterceptor);
  }
}

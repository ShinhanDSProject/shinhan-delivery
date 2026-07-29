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
   * 순환 빈 생성(WebSocketConfig → interceptor → TrackingService → SimpMessagingTemplate) 방지를 위해 지연
   * 주입한다.
   */
  public WebSocketConfig(@Lazy StompAuthChannelInterceptor stompAuthChannelInterceptor) {
    this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
  }

  /** WebSocket 핸드셰이크 진입점. 인증은 {@link StompAuthChannelInterceptor}가 CONNECT에서 처리한다. */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
  }

  /** {@code /topic}은 서버→클라이언트 브로드캐스트, {@code /app}은 클라이언트→서버 발행(컨트롤러 라우팅) prefix. */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

  /** REST의 JwtAuthenticationFilter에 대응하는 인증·인가 인터셉터를 인바운드 채널에 등록한다. */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompAuthChannelInterceptor);
  }
}

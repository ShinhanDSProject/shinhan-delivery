package com.example.shinhangaecheokja.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 모든 HTTP 요청에 고유 Trace ID를 부여해 SLF4J MDC에 주입하는 필터입니다.
 *
 * <p>요청 헤더 {@code X-Trace-Id}가 있으면 그 값을 그대로 사용하고, 없으면 새 UUID를 발급합니다. 요청 처리가 끝나면 {@link
 * MDC#clear()}로 반드시 정리하여, 스레드 풀이 다른 요청에 스레드를 재사용할 때 이전 요청의 traceId가 새어나가지 않도록 합니다.
 */
public class MdcLoggingFilter extends OncePerRequestFilter {

  private static final String TRACE_ID_HEADER = "X-Trace-Id";
  private static final String TRACE_ID_MDC_KEY = "traceId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    MDC.put(TRACE_ID_MDC_KEY, resolveTraceId(request));
    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  private String resolveTraceId(HttpServletRequest request) {
    String traceId = request.getHeader(TRACE_ID_HEADER);
    return StringUtils.hasText(traceId) ? traceId : UUID.randomUUID().toString();
  }
}

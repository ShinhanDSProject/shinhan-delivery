package com.example.shinhangaecheokja.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class MdcLoggingFilterTest {

  private final MdcLoggingFilter filter = new MdcLoggingFilter();

  @Test
  @DisplayName("요청 헤더에 X-Trace-Id가 있으면 해당 값을 그대로 MDC에 주입한다.")
  void usesTraceIdFromRequestHeaderWhenPresent() throws ServletException, IOException {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Trace-Id", "given-trace-id");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> traceIdDuringChain = new AtomicReference<>();

    // when
    filter.doFilter(request, response, (req, res) -> traceIdDuringChain.set(MDC.get("traceId")));

    // then
    assertThat(traceIdDuringChain.get()).isEqualTo("given-trace-id");
  }

  @Test
  @DisplayName("요청 헤더에 X-Trace-Id가 없으면 새 UUID를 생성해 MDC에 주입한다.")
  void generatesUuidWhenHeaderMissing() throws ServletException, IOException {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<String> traceIdDuringChain = new AtomicReference<>();

    // when
    filter.doFilter(request, response, (req, res) -> traceIdDuringChain.set(MDC.get("traceId")));

    // then
    assertThat(traceIdDuringChain.get()).isNotBlank();
    assertThatCode(() -> UUID.fromString(traceIdDuringChain.get())).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("요청 처리가 끝나면 MDC에서 traceId가 제거되어 스레드 재사용 시 누수되지 않는다.")
  void clearsMdcAfterRequestCompletes() throws ServletException, IOException {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    // when
    filter.doFilter(request, response, (req, res) -> {});

    // then
    assertThat(MDC.get("traceId")).isNull();
  }
}

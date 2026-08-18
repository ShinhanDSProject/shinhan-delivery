package com.example.shinhandelivery.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointWalletHistoryTemplateTest {

  @Test
  @DisplayName("포인트 지갑 최근 내역은 서버 API를 사용하고 세션 스냅샷에 의존하지 않는다")
  void pointWalletHistoryShouldUseServerApiInsteadOfSessionSnapshot() throws IOException {
    String walletTemplate = readResource("templates/point-wallet.html");
    String chargeTemplate = readResource("templates/point-charge.html");

    assertThat(walletTemplate)
        .contains("MyPageApi.request(\"/api/v1/points/histories\")")
        .contains("아직 포인트 이용 내역이 없습니다.")
        .doesNotContain("pointChargeSnapshot")
        .doesNotContain("sessionStorage.removeItem");
    assertThat(chargeTemplate).doesNotContain("pointChargeSnapshot");
  }

  private String readResource(String resourcePath) throws IOException {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      assertThat(inputStream).as("리소스 %s", resourcePath).isNotNull();
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}

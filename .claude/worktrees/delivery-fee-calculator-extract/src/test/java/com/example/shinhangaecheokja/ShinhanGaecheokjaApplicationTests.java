package com.example.shinhandelivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.member.secret=test-jwt-secret-key-for-application-context")
class ShinhanDeliveryApplicationTests {

  @Test
  void contextLoads() {}
}

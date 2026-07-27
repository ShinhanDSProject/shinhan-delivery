package com.example.shinhangaecheokja;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.member.secret=test-jwt-secret-key-for-application-context")
class ShinhanGaecheokjaApplicationTests {

    @Test
    void contextLoads() {
    }

}

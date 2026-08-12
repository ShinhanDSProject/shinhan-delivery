package com.example.shinhandelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShinhanDeliveryApplication {

  public static void main(String[] args) {
    SpringApplication.run(ShinhanDeliveryApplication.class, args);
  }
}

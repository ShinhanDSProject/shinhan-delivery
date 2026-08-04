package com.example.shinhandelivery.delivery.entity;

/** DeliveryRequest의 진행 상태. */
public enum DeliveryStatus {
  REQUESTED,
  MATCHED,
  PICKED_UP,
  COMPLETED,
  CANCELLED
}

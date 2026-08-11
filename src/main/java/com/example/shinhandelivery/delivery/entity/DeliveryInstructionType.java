package com.example.shinhandelivery.delivery.entity;

/** 배송 현장에서 물품을 전달하는 방식을 나타낸다. */
public enum DeliveryInstructionType {
  NONE,
  LEAVE_AT_DOOR_NO_BELL,
  ENTRANCE_CODE,
  SECURITY_OFFICE,
  CUSTOM
}

package com.example.shinhandelivery.member.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 실시간 검증 대상 회원 필드 구분 Enum. */
@Getter
@RequiredArgsConstructor
public enum MemberValidationField {
  EMAIL("email"),
  PHONE_NUMBER("phoneNumber"),
  PASSWORD("password");

  @JsonValue private final String value;

  @JsonCreator
  public static MemberValidationField fromValue(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    for (MemberValidationField field : values()) {
      if (field.value.equalsIgnoreCase(trimmed) || field.name().equalsIgnoreCase(trimmed)) {
        return field;
      }
    }
    return null;
  }
}

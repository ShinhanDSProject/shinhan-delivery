package com.example.shinhandelivery.member.dto.request;

import com.example.shinhandelivery.member.constant.MemberValidationConstants;
import com.example.shinhandelivery.member.entity.MemberRole;
import com.example.shinhandelivery.vehicle.entity.VehicleType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 생성 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class MemberCreateRequest {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "올바른 이메일 형식이어야 합니다.")
  private String email;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하이어야 합니다.")
  @Pattern(
      regexp = MemberValidationConstants.PASSWORD_REGEX,
      message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
  private String password;

  @NotBlank(message = "이름은 필수 입력값입니다.")
  private String name;

  @NotBlank(message = "전화번호는 필수 입력값입니다.")
  @Pattern(
      regexp = MemberValidationConstants.PHONE_REGEX,
      message = "올바른 전화번호 형식(예: 010-1234-5678)이어야 합니다.")
  private String phoneNumber;

  // 배송원에게만 필요한 정보들
  private MemberRole role = MemberRole.CUSTOMER;

  private VehicleType vehicleType;

  private String equipmentName;

  private String activityRegion;

  @DecimalMin(value = "0.1", message = "최대 적재 무게는 0보다 커야 합니다.")
  private Double preferredWeight;

  @DecimalMin(value = "0.1", message = "최대 주행 거리는 0보다 커야 합니다.")
  private Double maxDistance;

  private String proofDocumentUrl;

  // 이 메서드는 결과가 반드시 true여야 통과
  @AssertTrue(message = "배송원 회원가입 시 차량 종류, 최대 적재 무게, 자격 증빙 서류는 필수입니다.")
  public boolean isCourierProfileValid() {
    if (role != MemberRole.COURIER) {
      return true;
    }
    return vehicleType != null
        && preferredWeight != null
        && preferredWeight > 0
        && proofDocumentUrl != null
        && !proofDocumentUrl.isBlank();
  }
}

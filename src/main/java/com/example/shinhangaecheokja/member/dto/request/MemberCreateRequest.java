package com.example.shinhangaecheokja.member.dto.request;

import com.example.shinhangaecheokja.member.entity.MemberRole;
import com.example.shinhangaecheokja.vehicle.entity.VehicleType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 가입 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class MemberCreateRequest {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
  private String password;

  @NotBlank(message = "이름은 필수 입력값입니다.")
  private String name;

  @NotBlank(message = "전화번호는 필수 입력값입니다.")
  private String phoneNumber;

  private MemberRole role = MemberRole.CUSTOMER;

  private VehicleType vehicleType;

  private String activityRegion;

  private Double preferredWeight;

  /** 배송원 회원가입 요청인 경우 차량 종류/활동 지역/희망 중량을 모두 요구한다. */
  @AssertTrue(message = "배송원 회원가입에는 차량 종류, 활동 희망 지역, 희망 배송 중량이 모두 필요합니다.")
  public boolean isCourierProfileValid() {
    if (role != MemberRole.COURIER) {
      return true;
    }
    return vehicleType != null
        && activityRegion != null
        && !activityRegion.isBlank()
        && preferredWeight != null
        && preferredWeight > 0;
  }
}

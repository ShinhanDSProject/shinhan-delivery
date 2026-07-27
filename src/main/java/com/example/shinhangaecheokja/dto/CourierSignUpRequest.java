package com.example.shinhangaecheokja.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourierSignUpRequest {

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "이메일 주소(ID)는 필수 입력 항목입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String confirmPassword;

    @NotBlank(message = "휴대폰 번호는 필수 입력 항목입니다.")
    private String phoneNumber;

    @NotBlank(message = "운송 수단 선택은 필수 항목입니다.")
    private String vehicle;

    @NotBlank(message = "활동 희망 지역은 필수 입력 항목입니다.")
    private String activityRegion;

    @NotBlank(message = "희망 배송 중량 선택은 필수 항목입니다.")
    private String preferredWeight;

    @AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
    private boolean termsAgreed;

    @AssertTrue(message = "개인정보 수집 동의는 필수입니다.")
    private boolean privacyAgreed;

    @AssertTrue(message = "배송 안전수칙 동의는 필수입니다.")
    private boolean safetyAgreed;

    private boolean marketingAgreed;
}

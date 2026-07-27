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
public class SignUpRequest {

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

    @AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
    private boolean termsAgreed;

    @AssertTrue(message = "개인정보 수집 및 이용 동의는 필수입니다.")
    private boolean privacyAgreed;

    private boolean marketingAgreed;
}

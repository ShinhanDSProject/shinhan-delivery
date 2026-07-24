package com.example.shinhangaecheokja.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneSendRequest {

    @NotBlank(message = "휴대폰 번호는 필수 항목입니다.")
    private String phoneNumber;
}

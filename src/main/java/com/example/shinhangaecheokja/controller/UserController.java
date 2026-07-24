package com.example.shinhangaecheokja.controller;

import com.example.shinhangaecheokja.dto.PhoneSendRequest;
import com.example.shinhangaecheokja.dto.PhoneVerifyRequest;
import com.example.shinhangaecheokja.dto.SignUpRequest;
import com.example.shinhangaecheokja.dto.UserResponse;
import com.example.shinhangaecheokja.service.UserService;
import com.example.shinhangaecheokja.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final VerificationService verificationService;

    // 1. 휴대폰 인증번호 발송 API
    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, String>> sendCode(@Valid @RequestBody PhoneSendRequest request) {
        String code = verificationService.sendVerificationCode(request.getPhoneNumber());
        return ResponseEntity.ok(Map.of(
                "message", "인증번호가 발송되었습니다.",
                "code", code // 테스트 편의를 위해 응답에도 인증번호 반환
        ));
    }

    // 2. 휴대폰 인증번호 확인 API
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@Valid @RequestBody PhoneVerifyRequest request) {
        boolean isSuccess = verificationService.verifyCode(request.getPhoneNumber(), request.getCode());
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("success", true, "message", "휴대폰 인증이 완료되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "인증번호가 일치하지 않거나 만료되었습니다."));
        }
    }

    // 3. 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        UserResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

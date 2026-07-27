package com.example.shinhangaecheokja.controller;

import com.example.shinhangaecheokja.dto.CourierSignUpRequest;
import com.example.shinhangaecheokja.dto.SignUpRequest;
import com.example.shinhangaecheokja.dto.UserResponse;
import com.example.shinhangaecheokja.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")   //앞으로 회원과 관련된 모든 요청은 무조건 이쪽으로 찾아오세요!
@RequiredArgsConstructor    //UserService같은 필수 직원들을 스프링이 알아서 데스크에 배치(의존성 주입)
public class UserController {

    private final UserService userService;  //controller에서 직접하지 않고 userservice에게 넘김

    // 고객 회원가입 API
    @PostMapping("/signup") //데이터를 새로 생성하는 POST방식을 사용해,
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) { 
        UserResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 배송원 회원가입 API
    @PostMapping("/courier-signup")
    public ResponseEntity<UserResponse> signUpCourier(@Valid @RequestBody CourierSignUpRequest request) {
        UserResponse response = userService.signUpCourier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}


package com.example.shinhangaecheokja.controller;

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

    // 회원가입 API
    @PostMapping("/signup") //데이터를 새로 생성하는 POST방식을 사용해,
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody SignUpRequest request) { 
        //ResponseEntity<UserResponse> = 손님에게 보낼 최종 결과물의 규격, UserResponse상자를 돌려주겠다
        //@vaild = 1차 검사, 이메일등이 이상한 값이 들어오면 컷 해버리기
        //@RequsetBody = JSON데이터를 자바가 읽을 수 있는 객체로 변환
        UserResponse response = userService.signUp(request);
        //userService에서 작업을 한 후 UserResponse상자를 돌려줘
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        //프론트엔드에서 정확히 알 수 있도록 201 CREATED(새로운 데이터 생성!)라는 상태 코드 팻말을 세운뒤, response를 담아서 보냄.
    }
}


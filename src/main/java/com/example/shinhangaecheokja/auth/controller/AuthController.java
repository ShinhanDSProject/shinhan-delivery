package com.example.shinhangaecheokja.auth.controller;

import com.example.shinhangaecheokja.auth.dto.LoginRequest;
import com.example.shinhangaecheokja.auth.dto.LoginResponse;
import com.example.shinhangaecheokja.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

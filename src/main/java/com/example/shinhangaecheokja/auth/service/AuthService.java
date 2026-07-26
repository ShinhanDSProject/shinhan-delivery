package com.example.shinhangaecheokja.auth.service;

import com.example.shinhangaecheokja.auth.dto.LoginRequest;
import com.example.shinhangaecheokja.auth.dto.LoginResponse;
import com.example.shinhangaecheokja.auth.exception.InvalidLoginException;
import com.example.shinhangaecheokja.auth.jwt.JwtTokenProvider;
import com.example.shinhangaecheokja.entity.User;
import com.example.shinhangaecheokja.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(InvalidLoginException::new);

        // 프론트에서 입력한 비밀번호와 db에 있는 user의 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidLoginException();
        }

        return LoginResponse.builder()
                .accessToken(jwtTokenProvider.createAccessToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessExpirationSeconds())
                .build();
    }
}

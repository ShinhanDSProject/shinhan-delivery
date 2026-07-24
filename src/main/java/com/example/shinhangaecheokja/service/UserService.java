package com.example.shinhangaecheokja.service;

import com.example.shinhangaecheokja.dto.SignUpRequest;
import com.example.shinhangaecheokja.dto.UserResponse;
import com.example.shinhangaecheokja.entity.User;
import com.example.shinhangaecheokja.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;

    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        // 1. 비밀번호 일치 확인
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 2. 휴대폰 인증 완료 확인
        if (!verificationService.isPhoneVerified(request.getPhoneNumber())) {
            throw new IllegalArgumentException("휴대폰 번호 인증이 필요합니다.");
        }

        // 3. 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일 주소입니다.");
        }

        // 4. 비밀번호 암호화 및 유저 엔티티 생성
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.create(
                request.getName(),
                request.getEmail(),
                encodedPassword,
                request.getPhoneNumber(),
                request.isMarketingAgreed()
        );

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }
}

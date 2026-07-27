package com.example.shinhangaecheokja.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.shinhangaecheokja.auth.dto.LoginRequest;
import com.example.shinhangaecheokja.auth.dto.LoginResponse;
import com.example.shinhangaecheokja.auth.exception.InvalidLoginException;
import com.example.shinhangaecheokja.auth.jwt.JwtTokenProvider;
import com.example.shinhangaecheokja.entity.User;
import com.example.shinhangaecheokja.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void 이메일과_비밀번호가_일치하면_JWT_로그인에_성공한다() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = User.create("홍길동", request.getEmail(), "encodedPassword", "01012345678", false);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtTokenProvider.createAccessToken(user)).willReturn("access-token");
        given(jwtTokenProvider.getAccessExpirationSeconds()).willReturn(1800L);

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(1800L);
    }

    @Test
    void 가입되지_않은_이메일이면_InvalidLoginException을_던진다() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidLoginException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void 비밀번호가_일치하지_않으면_InvalidLoginException을_던진다() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong-password");
        User user = User.create("홍길동", request.getEmail(), "encodedPassword", "01012345678", false);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidLoginException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}

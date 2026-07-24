package com.example.shinhangaecheokja.service;

import com.example.shinhangaecheokja.dto.SignUpRequest;
import com.example.shinhangaecheokja.dto.UserResponse;
import com.example.shinhangaecheokja.entity.User;
import com.example.shinhangaecheokja.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUpSuccess() {
        // given
        SignUpRequest request = new SignUpRequest(
                "홍길동",
                "test@example.com",
                "password123",
                "password123",
                "01012345678",
                true,
                true,
                false
        );

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword123");

        User savedUser = User.create(
                request.getName(),
                request.getEmail(),
                "encodedPassword123",
                request.getPhoneNumber(),
                request.isMarketingAgreed()
        );
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        UserResponse response = userService.signUp(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 불일치 시 예외 발생 테스트")
    void signUpPasswordMismatchThrowsException() {
        // given
        SignUpRequest request = new SignUpRequest(
                "홍길동",
                "test@example.com",
                "password123",
                "wrongPassword",
                "01012345678",
                true,
                true,
                false
        );

        // when & then
        assertThatThrownBy(() -> userService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
    }
}


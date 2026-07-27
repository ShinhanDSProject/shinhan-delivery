package com.example.shinhangaecheokja.dto;

import com.example.shinhangaecheokja.entity.Role;
import com.example.shinhangaecheokja.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private boolean marketingAgreed;
    private Role role;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .marketingAgreed(user.isMarketingAgreed())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

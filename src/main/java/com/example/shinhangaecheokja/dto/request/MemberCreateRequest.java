package com.example.shinhangaecheokja.dto.request;

import com.example.shinhangaecheokja.entity.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberCreateRequest {

  private String email;
  private String password;
  private String name;
  private String phoneNumber;
  private MemberRole role;
}

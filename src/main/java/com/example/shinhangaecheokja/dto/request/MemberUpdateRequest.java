package com.example.shinhangaecheokja.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberUpdateRequest {

  private String name;
  private String phoneNumber;
}

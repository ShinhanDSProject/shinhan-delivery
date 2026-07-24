package com.example.shinhangaecheokja.delivery.dto.request;

import com.example.shinhangaecheokja.delivery.entity.MatchingStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 매칭 상태 변경 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class MatchingUpdateRequest {

  private MatchingStatus status;
}

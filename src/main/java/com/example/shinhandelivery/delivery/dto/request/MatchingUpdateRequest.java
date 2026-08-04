package com.example.shinhandelivery.delivery.dto.request;

import com.example.shinhandelivery.delivery.entity.MatchingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 매칭 상태 변경 요청 DTO. */
@Getter
@Setter
@NoArgsConstructor
public class MatchingUpdateRequest {

  @NotNull(message = "변경할 상태는 필수입니다.")
  private MatchingStatus status;
}

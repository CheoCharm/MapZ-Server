package com.cheocharm.MapZ.user.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenPairResponseDto {
    private String accessToken;
    private String refreshToken;
}

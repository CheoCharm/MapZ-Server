package com.cheocharm.MapZ.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenPairResponse {
    private String accessToken;
    private String refreshToken;
}

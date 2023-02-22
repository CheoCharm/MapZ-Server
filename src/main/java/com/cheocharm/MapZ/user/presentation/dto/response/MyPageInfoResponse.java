package com.cheocharm.MapZ.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageInfoResponse {
    private String username;
    private String userImageUrl;
}

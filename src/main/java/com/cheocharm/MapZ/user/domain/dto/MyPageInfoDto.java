package com.cheocharm.MapZ.user.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageInfoDto {
    private String username;
    private String userImageUrl;
}

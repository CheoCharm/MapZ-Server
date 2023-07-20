package com.cheocharm.MapZ.user.presentation.dto.response;

import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MyPageInfoResponse {
    private String username;
    private String userImageUrl;

    public static MyPageInfoResponse from(UserEntity user) {
        return new MyPageInfoResponse(user.getUsername(), user.getUserImageUrl());
    }
}

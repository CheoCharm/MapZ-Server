package com.cheocharm.MapZ.user.presentation.dto.response;

import com.cheocharm.MapZ.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MyPageInfoResponse {
    private String username;
    private String userImageUrl;

    public static MyPageInfoResponse from(User user) {
        return new MyPageInfoResponse(user.getUsername(), user.getUserImageUrl());
    }
}

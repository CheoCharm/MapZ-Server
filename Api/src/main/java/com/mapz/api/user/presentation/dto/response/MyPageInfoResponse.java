package com.mapz.api.user.presentation.dto.response;

import com.mapz.domain.domains.user.entity.User;
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

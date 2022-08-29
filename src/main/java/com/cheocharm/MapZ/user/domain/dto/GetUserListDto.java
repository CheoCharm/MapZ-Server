package com.cheocharm.MapZ.user.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetUserListDto {
    private Boolean hasNext;
    private List<UserList> userList;

    @Getter
    @Builder
    public static class UserList {
        private String username;
        private String userImageUrl;
    }

}

package com.cheocharm.MapZ.user.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetUserListDto {
    Boolean hasNext;
    List<UserList> userList;

    @Getter
    @Builder
    public static class UserList {
        String username;
        String userImageUrl;
    }

}

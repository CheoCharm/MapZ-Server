package com.cheocharm.MapZ.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "그룹에 포함되어 있는 멤버면 true, 아니면 false")
        private Boolean member;
    }

}

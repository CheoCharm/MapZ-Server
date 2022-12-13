package com.cheocharm.MapZ.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetUserListDto {
    private Boolean hasNext;
    private List<UserList> userList;

    @Getter
    @Builder
    public static class UserList {
        private String username;
        private String userImageUrl;
        private Long userId;
        @Schema(description = "그룹에 포함되어 있는 멤버면 true, 아니면 false")
        private Boolean member;
    }

}

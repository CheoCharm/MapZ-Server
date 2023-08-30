package com.mapz.api.user.presentation.dto.response;

import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.usergroup.entity.UserGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class GetUserListResponse {
    private Boolean hasNext;
    private List<UserList> userList;

    @Getter
    @Builder
    public static class UserList {
        private String username;
        private String userImageUrl;
        private Long userId;
        @Schema(description = "그룹에 포함되어 있는 멤버면 true, 아니면 false")
        private Boolean isMember;

    }

    public static GetUserListResponse of(List<User> userEntities, List<UserGroup> userGroupEntities, boolean hasNext) {
        final List<GetUserListResponse.UserList> userList = userEntities.stream()
                .map(userEntity ->
                        GetUserListResponse.UserList.builder()
                                .username(userEntity.getUsername())
                                .userImageUrl(userEntity.getUserImageUrl())
                                .userId(userEntity.getId())
                                .isMember(userGroupEntities.stream()
                                        .anyMatch(userGroupEntity -> userEntity.equals(userGroupEntity.getUser()))
                                )
                                .build()
                )
                .collect(Collectors.toList());

        return new GetUserListResponse(hasNext, userList);
    }
}

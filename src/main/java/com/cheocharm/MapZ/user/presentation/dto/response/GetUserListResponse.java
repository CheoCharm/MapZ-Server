package com.cheocharm.MapZ.user.presentation.dto.response;

import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.domain.UserGroupEntity;
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
        private Boolean member;

    }

    public static GetUserListResponse of(List<UserEntity> userEntities, List<UserGroupEntity> userGroupEntities, boolean hasNext) {
        final List<GetUserListResponse.UserList> userList = userEntities.stream()
                .map(userEntity ->
                        GetUserListResponse.UserList.builder()
                                .username(userEntity.getUsername())
                                .userImageUrl(userEntity.getUserImageUrl())
                                .userId(userEntity.getId())
                                .member(isMember(userEntity, userGroupEntities))
                                .build()
                )
                .collect(Collectors.toList());

        return new GetUserListResponse(hasNext, userList);
    }

    private static Boolean isMember(UserEntity userEntity, List<UserGroupEntity> userGroupEntities) {
        for (UserGroupEntity userGroupEntity : userGroupEntities) {
            if (userEntity.equals(userGroupEntity.getUserEntity())) {
                return true;
            }
        }
        return false;
    }
}

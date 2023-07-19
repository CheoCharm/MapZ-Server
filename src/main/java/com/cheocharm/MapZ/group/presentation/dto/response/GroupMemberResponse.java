package com.cheocharm.MapZ.group.presentation.dto.response;

import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import com.cheocharm.MapZ.usergroup.domain.UserGroupEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class GroupMemberResponse {
    private String username;
    private String userImageUrl;
    private Long userId;
    private InvitationStatus invitationStatus;

    public static List<GroupMemberResponse> of(List<UserGroupEntity> userGroupEntities) {
        return userGroupEntities.stream()
                .map(userGroupEntity ->
                        {
                            UserEntity user = userGroupEntity.getUserEntity();
                            return GroupMemberResponse.builder()
                                    .username(user.getUsername())
                                    .userImageUrl(user.getUserImageUrl())
                                    .userId(user.getId())
                                    .invitationStatus(userGroupEntity.getInvitationStatus())
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }
}

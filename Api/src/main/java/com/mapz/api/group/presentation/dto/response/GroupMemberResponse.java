package com.mapz.api.group.presentation.dto.response;

import com.mapz.domain.domains.usergroup.enums.InvitationStatus;
import com.mapz.domain.domains.usergroup.entity.UserGroup;
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

    public static List<GroupMemberResponse> of(List<UserGroup> userGroupEntities) {
        return userGroupEntities.stream()
                .map(userGroupEntity ->
                        GroupMemberResponse.builder()
                                    .username(userGroupEntity.getUser().getUsername())
                                    .userImageUrl(userGroupEntity.getUser().getUserImageUrl())
                                    .userId(userGroupEntity.getUser().getId())
                                    .invitationStatus(userGroupEntity.getInvitationStatus())
                                    .build()
                )
                .collect(Collectors.toList());
    }
}

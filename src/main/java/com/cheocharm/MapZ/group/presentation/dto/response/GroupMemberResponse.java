package com.cheocharm.MapZ.group.presentation.dto.response;

import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GroupMemberResponse {
    private String username;
    private String userImageUrl;
    private Long userId;
    private InvitationStatus invitationStatus;
}

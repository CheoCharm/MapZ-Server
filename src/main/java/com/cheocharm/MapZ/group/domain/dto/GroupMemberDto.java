package com.cheocharm.MapZ.group.domain.dto;

import com.cheocharm.MapZ.usergroup.InvitationStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GroupMemberDto {
    private String username;
    private String userImageUrl;
    private InvitationStatus invitationStatus;
}

package com.cheocharm.MapZ.group.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyInvitationResponse {

    private boolean hasNext;
    private List<Invitation> invitationList;

    @Getter
    @AllArgsConstructor
    public static class Invitation {
        private Long groupId;
        private String userName;
        private LocalDateTime createdAt;
    }
}

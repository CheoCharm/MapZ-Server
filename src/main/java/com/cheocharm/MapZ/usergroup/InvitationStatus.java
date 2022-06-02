package com.cheocharm.MapZ.usergroup;

import lombok.Getter;

@Getter
public enum InvitationStatus {
    PENDING("대기중"), ACCEPT("수락완료");

    private final String status;

    InvitationStatus(String status) {
        this.status = status;
    }
}

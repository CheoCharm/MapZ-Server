package com.mapz.domain.domains.usergroup.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    CHIEF("그룹장"), MEMBER("그룹원");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }
}

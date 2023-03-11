package com.cheocharm.MapZ.group.domain;

import lombok.Getter;

@Getter
public enum GroupLimit {
    LIMIT_GROUP_PEOPLE(10),
    ;
    private final int limitSize;

    GroupLimit(int limitSize) {
        this.limitSize = limitSize;
    }
}

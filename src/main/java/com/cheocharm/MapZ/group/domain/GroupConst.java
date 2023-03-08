package com.cheocharm.MapZ.group.domain;

import lombok.Getter;

@Getter
public enum GroupConst {
    LIMIT_GROUP_PEOPLE(10),
    ;
    private final int limitSize;

    GroupConst(int limitSize) {
        this.limitSize = limitSize;
    }
}

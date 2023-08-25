package com.cheocharm.MapZ.group.domain;

import lombok.Getter;

@Getter
public enum GroupLimit {
    LIMIT_GROUP_PEOPLE(10L),
    ;
    private final Long limitSize;

    GroupLimit(Long limitSize) {
        this.limitSize = limitSize;
    }
}

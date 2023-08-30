package com.mapz.domain.domains.usergroup.vo;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class CountUserGroupVO {
    private Long cnt;
    private Long id;

    @QueryProjection
    public CountUserGroupVO(Long cnt, Long id) {
        this.cnt = cnt;
        this.id = id;
    }
}

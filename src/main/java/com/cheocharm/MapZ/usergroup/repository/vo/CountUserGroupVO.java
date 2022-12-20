package com.cheocharm.MapZ.usergroup.repository.vo;

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

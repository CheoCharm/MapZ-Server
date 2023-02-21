package com.cheocharm.MapZ.usergroup.domain.repository.vo;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class ChiefUserImageVO {
    private String chiefUserImage;
    private Long id;

    @QueryProjection
    public ChiefUserImageVO(String chiefUserImage, Long id) {
        this.chiefUserImage = chiefUserImage;
        this.id = id;
    }
}

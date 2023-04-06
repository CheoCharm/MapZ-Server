package com.cheocharm.MapZ.diary.domain.repository.vo;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class DiaryPreviewVO {
    private String diaryImageURL;
    private Integer imageOrder;
    private String address;
    private boolean isLike;

    @QueryProjection
    public DiaryPreviewVO(String diaryImageURL, Integer imageOrder, String address, boolean isLike) {
        this.diaryImageURL = diaryImageURL;
        this.imageOrder = imageOrder;
        this.address = address;
        this.isLike = isLike;
    }
}

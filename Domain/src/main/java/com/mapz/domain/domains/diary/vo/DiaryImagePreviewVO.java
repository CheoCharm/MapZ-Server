package com.mapz.domain.domains.diary.vo;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class DiaryImagePreviewVO {
    private Long diaryId;
    private String mainDiaryImageURL;

    @QueryProjection
    public DiaryImagePreviewVO(Long diaryId, String mainDiaryImageURL) {
        this.diaryId = diaryId;
        this.mainDiaryImageURL = mainDiaryImageURL;
    }
}

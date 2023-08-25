package com.cheocharm.MapZ.diary.domain.repository.vo;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyDiaryVO {
    private String title;
    private LocalDateTime createdAt;
    private String imageUrl;
    private Long groupId;
    private Long diaryId;
    private Long commentCount;

    @QueryProjection
    public MyDiaryVO(String title, LocalDateTime createdAt, String imageUrl, Long groupId, Long diaryId, Long commentCount) {
        this.title = title;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.groupId = groupId;
        this.diaryId = diaryId;
        this.commentCount = commentCount;
    }
}

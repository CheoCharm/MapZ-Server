package com.cheocharm.MapZ.diary.domain.respository.vo;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyLikeDiaryVO {
    private String title;
    private LocalDateTime createdAt;
    private String imageUrl;
    private Long groupId;
    private Long diaryId;
    private Long commentCount;

    @QueryProjection
    public MyLikeDiaryVO(String title, LocalDateTime createdAt, String imageUrl, Long groupId, Long diaryId, Long commentCount) {
        this.title = title;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
        this.groupId = groupId;
        this.diaryId = diaryId;
        this.commentCount = commentCount;
    }
}

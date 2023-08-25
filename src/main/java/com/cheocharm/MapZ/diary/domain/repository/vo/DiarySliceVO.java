package com.cheocharm.MapZ.diary.domain.repository.vo;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DiarySliceVO {
    private Long diaryId;
    private String title;
    private String content;
    private String address;
    private LocalDateTime createdAt;
    private String username;
    private String userImageURL;
    private Integer likeCount;
    private boolean isLike;
    private Long commentCount;
    private boolean isWriter;


    @QueryProjection
    public DiarySliceVO(Long diaryId, String title, String content, String address, LocalDateTime createdAt, String username, String userImageURL, Integer likeCount, boolean isLike, Long commentCount, boolean isWriter) {
        this.diaryId = diaryId;
        this.title = title;
        this.content = content;
        this.address = address;
        this.createdAt = createdAt;
        this.username = username;
        this.userImageURL = userImageURL;
        this.likeCount = likeCount;
        this.isLike = isLike;
        this.commentCount = commentCount;
        this.isWriter = isWriter;
    }
}

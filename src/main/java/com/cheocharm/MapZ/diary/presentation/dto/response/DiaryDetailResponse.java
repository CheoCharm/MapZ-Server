package com.cheocharm.MapZ.diary.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DiaryDetailResponse {
    private String title;
    private String content;
    private String address;
    private LocalDateTime createdAt;
    private String username;
    private String userImageURL;
    private Integer likeCount;
    private boolean isLike;
    private Integer commentCount;

}

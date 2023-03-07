package com.cheocharm.MapZ.diary.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public class GetDiaryListResponse {

    private boolean hasNext;
    private List<DiaryList> diaryList;

    @Getter
    @AllArgsConstructor
    public static class DiaryList {
        private Long diaryId;
        private String title;
        private String contentText;
        private String address;
        private LocalDateTime createdAt;
        private String username;
        private String userImageURL;
        private Integer likeCount;
        private Boolean isLike;
        private Long commentCount;
        private Boolean isWriter;
    }

}

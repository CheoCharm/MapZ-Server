package com.cheocharm.MapZ.like.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyLikeDiaryResponse {

    private boolean hasNext;
    private List<Diary> diaryList;

    @Getter
    @Builder
    public static class Diary {
        private String title;
        private String createdAt;
        private String ImageUrl;
        private Long groupId;
        private Long diaryId;
        private Long commentCount;
    }
}

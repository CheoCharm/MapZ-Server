package com.cheocharm.MapZ.diary.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyDiaryDto {

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
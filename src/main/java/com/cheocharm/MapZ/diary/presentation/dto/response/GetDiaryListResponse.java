package com.cheocharm.MapZ.diary.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class GetDiaryListResponse {

    private boolean hasNext;
    private List<DiaryList> diaryList;

    @Getter
    @Builder
    public static class DiaryList {
        private Double x;
        private Double y;
        private Long diaryId;
        private String title;
        private String content;
    }

}
package com.cheocharm.MapZ.diary.presentation.dto.response;

import lombok.Getter;

@Getter
public class WriteDiaryResponse {
    private final Long diaryId;

    public WriteDiaryResponse(Long diaryId) {
        this.diaryId = diaryId;
    }
}

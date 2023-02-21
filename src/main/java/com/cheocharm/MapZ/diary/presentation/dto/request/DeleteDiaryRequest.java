package com.cheocharm.MapZ.diary.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class DeleteDiaryRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long diaryId;
}

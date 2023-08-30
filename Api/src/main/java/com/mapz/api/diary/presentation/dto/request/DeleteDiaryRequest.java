package com.mapz.api.diary.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class DeleteDiaryRequest {

    @NotNull
    private Long diaryId;
}

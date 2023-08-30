package com.mapz.api.diary.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class WriteDiaryRequest {

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private Long diaryId;
}

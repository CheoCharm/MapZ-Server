package com.cheocharm.MapZ.diary.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class WriteDiaryRequest {

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private Long diaryId;
}

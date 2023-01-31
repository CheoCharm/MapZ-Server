package com.cheocharm.MapZ.diary.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class DeleteDiaryDto {
    @NotNull
    private Long userId;

    @NotNull
    private Long diaryId;
}

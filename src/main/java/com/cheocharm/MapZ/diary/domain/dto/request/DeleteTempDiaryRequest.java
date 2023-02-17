package com.cheocharm.MapZ.diary.domain.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class DeleteTempDiaryRequest {
    @NotNull
    private Long diaryId;
}

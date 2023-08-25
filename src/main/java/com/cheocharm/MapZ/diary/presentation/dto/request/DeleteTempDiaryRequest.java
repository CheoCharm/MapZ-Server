package com.cheocharm.MapZ.diary.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class DeleteTempDiaryRequest {
    @NotNull
    private Long diaryId;
}

package com.cheocharm.MapZ.diary.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class LikeDiaryDto {

    @NotNull
    private Long diaryId;
}

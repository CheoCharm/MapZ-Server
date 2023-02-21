package com.cheocharm.MapZ.like.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class LikeDiaryRequest {

    @NotNull
    private Long diaryId;
}

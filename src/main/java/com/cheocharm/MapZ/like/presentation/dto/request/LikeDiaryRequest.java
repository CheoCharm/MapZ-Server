package com.cheocharm.MapZ.like.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
public class LikeDiaryRequest {

    @NotNull
    private Long diaryId;
}

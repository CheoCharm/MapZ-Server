package com.cheocharm.MapZ.diary.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class WriteDiaryDto {

    @NotNull
    private String title;

    @NotNull
    private String content;

    @NotNull
    private Long groupId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}

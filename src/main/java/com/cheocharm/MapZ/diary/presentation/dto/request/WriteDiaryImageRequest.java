package com.cheocharm.MapZ.diary.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class WriteDiaryImageRequest {
    @NotNull
    private Long groupId;

    @NotNull
    private String address;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}

package com.cheocharm.MapZ.diary.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DiaryCoordinateResponse {
    private Long diaryId;
    private Double longitude;
    private Double latitude;
}

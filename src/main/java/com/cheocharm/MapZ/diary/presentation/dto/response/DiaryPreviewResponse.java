package com.cheocharm.MapZ.diary.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DiaryPreviewResponse {
    private Long diaryId;
    private String previewImageURL;
    private Double longitude;
    private Double latitude;
}

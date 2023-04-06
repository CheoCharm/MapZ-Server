package com.cheocharm.MapZ.diary.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class DiaryPreviewDetailResponse {
    private List<ImageInfo> diaryImageURLs;
    private String address;
    private boolean isLike;

    @AllArgsConstructor
    @Getter
    public static class ImageInfo {
        private String diaryImageURL;
        private Integer imageOrder;
    }

}

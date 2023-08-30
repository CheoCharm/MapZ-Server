package com.mapz.api.diary.presentation.dto.response;

import com.mapz.domain.domains.diary.vo.DiaryPreviewVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

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

    public static DiaryPreviewDetailResponse of(List<DiaryPreviewVO> diaryPreviewVOS) {
        final List<DiaryPreviewDetailResponse.ImageInfo> list = diaryPreviewVOS.stream()
                .map(diaryPreviewVO -> new DiaryPreviewDetailResponse.ImageInfo(
                        diaryPreviewVO.getDiaryImageURL(),
                        diaryPreviewVO.getImageOrder()
                ))
                .collect(Collectors.toUnmodifiableList());

        final DiaryPreviewVO diaryPreviewVO = diaryPreviewVOS.get(0);
        return new DiaryPreviewDetailResponse(list, diaryPreviewVO.getAddress(), diaryPreviewVO.isLike());
    }
}

package com.cheocharm.MapZ.diary.presentation.dto.response;

import com.cheocharm.MapZ.common.util.ParserUtils;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiarySliceVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class GetDiaryListResponse {

    private boolean hasNext;
    private List<DiaryList> diaryList;

    @Getter
    @AllArgsConstructor
    public static class DiaryList {
        private Long diaryId;
        private String title;
        private String contentText;
        private String address;
        private LocalDateTime createdAt;
        private String username;
        private String userImageURL;
        private Integer likeCount;
        private Boolean isLike;
        private Long commentCount;
        private Boolean isWriter;
    }

    public static GetDiaryListResponse of(boolean hasNext, List<DiarySliceVO> diaries) {
        final List<GetDiaryListResponse.DiaryList> list = diaries.stream()
                .map(diarySliceVO -> new GetDiaryListResponse.DiaryList(
                        diarySliceVO.getDiaryId(),
                        diarySliceVO.getTitle(),
                        ParserUtils.getTextFromContent(diarySliceVO.getContent()),
                        diarySliceVO.getAddress(),
                        diarySliceVO.getCreatedAt(),
                        diarySliceVO.getUsername(),
                        diarySliceVO.getUserImageURL(),
                        diarySliceVO.getLikeCount(),
                        diarySliceVO.isLike(),
                        diarySliceVO.getCommentCount(),
                        diarySliceVO.isWriter()
                ))
                .collect(Collectors.toList());
        return new GetDiaryListResponse(hasNext, list);
    }
}

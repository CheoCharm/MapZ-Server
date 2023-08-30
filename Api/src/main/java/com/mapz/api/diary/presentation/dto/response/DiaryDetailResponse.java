package com.mapz.api.diary.presentation.dto.response;

import com.mapz.domain.domains.diary.vo.DiaryDetailVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DiaryDetailResponse {
    private String title;
    private String content;
    private String address;
    private LocalDateTime createdAt;
    private String username;
    private String userImageURL;
    private Integer likeCount;
    private Boolean isLike;
    private Long commentCount;
    private Boolean isWriter;

    public static DiaryDetailResponse of(DiaryDetailVO diaryDetail) {
        return new DiaryDetailResponse(diaryDetail.getTitle(),
                diaryDetail.getContent(),
                diaryDetail.getAddress(),
                diaryDetail.getCreatedAt(),
                diaryDetail.getUsername(),
                diaryDetail.getUserImageURL(),
                diaryDetail.getLikeCount(),
                diaryDetail.isLike(),
                diaryDetail.getCommentCount(),
                diaryDetail.isWriter()
        );
    }

}

package com.cheocharm.MapZ.like.presentation.dto.response;

import com.cheocharm.MapZ.like.domain.DiaryLike;
import com.cheocharm.MapZ.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class DiaryLikePeopleResponse {
    private String userImageUrl;
    private String username;

    public static List<DiaryLikePeopleResponse> of(List<DiaryLike> diaryLikeEntities) {
        return diaryLikeEntities.stream()
                .map(diaryLikeEntity -> DiaryLikePeopleResponse.builder()
                        .userImageUrl(diaryLikeEntity.getUser().getUserImageUrl())
                        .username(diaryLikeEntity.getUser().getUsername())
                        .build()
                )
                .collect(Collectors.toList());
    }
}

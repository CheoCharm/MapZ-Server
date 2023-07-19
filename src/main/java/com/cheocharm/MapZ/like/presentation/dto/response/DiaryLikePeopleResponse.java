package com.cheocharm.MapZ.like.presentation.dto.response;

import com.cheocharm.MapZ.like.domain.DiaryLikeEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class DiaryLikePeopleResponse {
    private String userImageUrl;
    private String username;

    public static List<DiaryLikePeopleResponse> of(List<DiaryLikeEntity> diaryLikeEntities) {
        return diaryLikeEntities.stream()
                .map(diaryLikeEntity -> {
                            UserEntity userEntity = diaryLikeEntity.getUserEntity();
                            return DiaryLikePeopleResponse.builder()
                                    .userImageUrl(userEntity.getUserImageUrl())
                                    .username(userEntity.getUsername())
                                    .build();
                        }
                )
                .collect(Collectors.toList());
    }
}

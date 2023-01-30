package com.cheocharm.MapZ.diary.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryLikePeopleDto {
    private String userImageUrl;
    private String username;
}

package com.cheocharm.MapZ.like.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryLikePeopleResponse {
    private String userImageUrl;
    private String username;
}

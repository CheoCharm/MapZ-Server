package com.mapz.api.group.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class JoinGroupResultResponse {
    private Boolean alreadyJoin;
    private String status;
}

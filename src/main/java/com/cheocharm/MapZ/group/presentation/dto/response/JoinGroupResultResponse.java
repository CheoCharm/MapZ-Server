package com.cheocharm.MapZ.group.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinGroupResultResponse {
    private Boolean alreadyJoin;
    private String status;
}

package com.cheocharm.MapZ.group.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinGroupResultDto {
    private Boolean alreadyJoin;
    private String status;
}

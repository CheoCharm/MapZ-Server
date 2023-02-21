package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
public class InviteGroupRequest {

    @NotNull
    private Long groupId;

    @NotNull
    private List<Long> userIdList;
}
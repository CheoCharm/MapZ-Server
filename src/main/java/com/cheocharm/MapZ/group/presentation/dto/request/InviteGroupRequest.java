package com.cheocharm.MapZ.group.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@Getter
public class InviteGroupRequest {

    @NotNull
    private Long groupId;

    @NotNull
    private List<Long> userIdList;
}

package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
public class InviteUserListDto {

    @NotNull
    private Long groupId;

    @NotNull
    private List<Long> userIdList;
}

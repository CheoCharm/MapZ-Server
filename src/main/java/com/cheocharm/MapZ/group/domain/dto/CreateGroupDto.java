package com.cheocharm.MapZ.group.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
public class CreateGroupDto {

    @NotBlank
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]{2,15}$", message = "그룹명은 공백을 제외한 특수문자를 포함하지 않는 2~15자리")
    private String groupName;

    @NotNull
    private String bio;

    @NotNull
    private Boolean changeStatus;
}

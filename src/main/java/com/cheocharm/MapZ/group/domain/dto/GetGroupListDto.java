package com.cheocharm.MapZ.group.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetGroupListDto {

    private String groupName;
    private String groupImageUrl;
    private Long count;
    private String chiefUserImage;

}

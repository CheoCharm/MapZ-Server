package com.cheocharm.MapZ.group.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetMyGroupResponse {

    private String groupName;
    private String groupImageUrl;
    private Long count;
    private String chiefUserImage;
    private Long groupId;
}

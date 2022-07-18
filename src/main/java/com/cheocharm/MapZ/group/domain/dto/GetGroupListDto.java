package com.cheocharm.MapZ.group.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetGroupListDto {

    String groupName;
    String groupImageUrl;
    int count;
    List<String> userImageUrlList;
}

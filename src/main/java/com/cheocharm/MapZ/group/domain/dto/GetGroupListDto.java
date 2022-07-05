package com.cheocharm.MapZ.group.domain.dto;

import lombok.Builder;

import java.util.List;

@Builder
public class GetGroupListDto {

    String groupImageUrl;
    int count;
    List<String> userImageUrlList;
}

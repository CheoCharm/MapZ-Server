package com.cheocharm.MapZ.group.domain.dto;

import lombok.Builder;

import java.util.List;

@Builder
public class GroupListDto {

    String groupImageUrl;
    int count;
    List<String> userImageUrlList;
}

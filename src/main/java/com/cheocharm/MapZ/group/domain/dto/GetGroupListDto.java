package com.cheocharm.MapZ.group.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetGroupListDto {

    Boolean hasNextPage;
    List<GroupList> groupList;

    @Getter
    @Builder
    public static class GroupList {
        String groupName;
        String groupImageUrl;
        String bio;
        String createdAt;
        int count;
        List<String> userImageUrlList;
    }
}

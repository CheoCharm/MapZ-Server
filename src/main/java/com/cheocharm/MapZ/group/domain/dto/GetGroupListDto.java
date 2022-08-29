package com.cheocharm.MapZ.group.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetGroupListDto {

    private Boolean hasNextPage;
    private List<GroupList> groupList;

    @Getter
    @Builder
    public static class GroupList {
        private String groupName;
        private String groupImageUrl;
        private int count;
        private List<String> userImageUrlList;
    }
}

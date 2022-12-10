package com.cheocharm.MapZ.group.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagingGetGroupListDto {

    private Boolean hasNextPage;
    private List<GroupList> groupList;

    @Getter
    @Builder
    public static class GroupList {
        private String groupName;
        private String groupImageUrl;
        private String bio;
        private String createdAt;
        private int count;
        private List<String> userImageUrlList;
    }
}

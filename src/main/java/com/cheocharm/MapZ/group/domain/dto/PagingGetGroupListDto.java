package com.cheocharm.MapZ.group.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagingGetGroupListDto {

    private Boolean hasNextPage;
    private List<GroupList> groupList;

    @Getter
    @Builder
    public static class GroupList {
        private String groupName;
        private String groupImageUrl;
        private Long groupId;
        private String bio;
        private String createdAt;
        private int count;
        private List<String> userImageUrlList;
    }
}

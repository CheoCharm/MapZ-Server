package com.cheocharm.MapZ.group.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagingGroupListResponse {

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
        private Long count;
        private String chiefUserImage;
    }
}

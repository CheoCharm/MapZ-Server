package com.mapz.api.group.presentation.dto.response;

import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.usergroup.vo.ChiefUserImageVO;
import com.mapz.domain.domains.usergroup.vo.CountUserGroupVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    public static PagingGroupListResponse of(List<Group> groupEntities, boolean hasNext,
                                             List<CountUserGroupVO> countUserGroupVOS,
                                             List<ChiefUserImageVO> chiefUserImageVOS) {
        List<PagingGroupListResponse.GroupList> groupList = groupEntities.stream()
                .map(groupEntity ->
                        PagingGroupListResponse.GroupList.builder()
                                .groupName(groupEntity.getGroupName())
                                .groupImageUrl(groupEntity.getGroupImageUrl())
                                .bio(groupEntity.getBio())
                                .createdAt(groupEntity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                                .chiefUserImage(getChiefUserImage(groupEntity, chiefUserImageVOS))
                                .count(getCount(groupEntity, countUserGroupVOS))
                                .groupId(groupEntity.getId())
                                .build()
                )
                .collect(Collectors.toList());

        return new PagingGroupListResponse(hasNext, groupList);
    }

    private static Long getCount(Group group, List<CountUserGroupVO> countUserGroupVOS) {
        Long count = 1L;
        for (CountUserGroupVO countUserGroupVO : countUserGroupVOS) {
            if (group.getId().equals(countUserGroupVO.getId())) {
                count = countUserGroupVO.getCnt();
                break;
            }
        }
        return count - 1;
    }

    private static String getChiefUserImage(Group group, List<ChiefUserImageVO> chiefUserImageVOS) {
        for (ChiefUserImageVO chiefUserImageVO : chiefUserImageVOS) {
            if (group.getId().equals(chiefUserImageVO.getId())) {
                return chiefUserImageVO.getChiefUserImage();
            }
        }
        return null;
    }
}

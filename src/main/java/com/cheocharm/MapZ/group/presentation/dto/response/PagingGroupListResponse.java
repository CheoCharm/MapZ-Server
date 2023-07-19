package com.cheocharm.MapZ.group.presentation.dto.response;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.CountUserGroupVO;
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

    public static PagingGroupListResponse of(List<GroupEntity> groupEntities, boolean hasNext,
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

    private static Long getCount(GroupEntity groupEntity, List<CountUserGroupVO> countUserGroupVOS) {
        Long count = 1L;
        for (CountUserGroupVO countUserGroupVO : countUserGroupVOS) {
            if (groupEntity.getId().equals(countUserGroupVO.getId())) {
                count = countUserGroupVO.getCnt();
                break;
            }
        }
        return count - 1;
    }

    private static String getChiefUserImage(GroupEntity groupEntity, List<ChiefUserImageVO> chiefUserImageVOS) {
        for (ChiefUserImageVO chiefUserImageVO : chiefUserImageVOS) {
            if (groupEntity.getId().equals(chiefUserImageVO.getId())) {
                return chiefUserImageVO.getChiefUserImage();
            }
        }
        return null;
    }
}

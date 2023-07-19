package com.cheocharm.MapZ.group.presentation.dto.response;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.CountUserGroupVO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class GetMyGroupResponse {

    private String groupName;
    private String groupImageUrl;
    private Long count;
    private String chiefUserImage;
    private Long groupId;


    public static List<GetMyGroupResponse> of(List<GroupEntity> groupEntities, List<CountUserGroupVO> countUserGroupVOS, List<ChiefUserImageVO> chiefUserImageVOS) {
        return groupEntities.stream()
                .map(groupEntity ->
                        GetMyGroupResponse.builder()
                                .groupName(groupEntity.getGroupName())
                                .groupImageUrl(groupEntity.getGroupImageUrl())
                                .groupId(groupEntity.getId())
                                .count(getCount(groupEntity,countUserGroupVOS))
                                .chiefUserImage(getChiefUserImage(groupEntity, chiefUserImageVOS))
                                .build()
                )
                .collect(Collectors.toList());
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

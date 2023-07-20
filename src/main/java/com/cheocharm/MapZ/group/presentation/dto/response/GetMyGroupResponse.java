package com.cheocharm.MapZ.group.presentation.dto.response;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.CountUserGroupVO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
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
                                .count(getCount(groupEntity.getId(), countUserGroupVOS))
                                .chiefUserImage(getChiefUserImage(groupEntity.getId(), chiefUserImageVOS)
                                        .orElse(null))
                                .build()
                )
                .collect(Collectors.toList());
    }

    private static Long getCount(Long id, List<CountUserGroupVO> countUserGroupVOS) {
        return countUserGroupVOS.stream()
                .filter(vo -> id.equals(vo.getId()))
                .map(CountUserGroupVO::getCnt)
                .findAny()
                .orElse(1L) - 1;
    }

    private static Optional<String> getChiefUserImage(Long id, List<ChiefUserImageVO> chiefUserImageVOS) {
        return chiefUserImageVOS.stream()
                .filter(vo -> id.equals(vo.getId()))
                .map(ChiefUserImageVO::getChiefUserImage)
                .findAny();
    }
}

package com.cheocharm.MapZ.usergroup.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.repository.vo.CountUserGroupVO;

import java.util.List;
import java.util.Optional;

public interface UserGroupRepositoryCustom {

    List<GroupEntity> getGroupEntityList(UserEntity userEntity);

    List<ChiefUserImageVO> findChiefUserImage(List<GroupEntity> groupEntityList);

    List<UserGroupEntity> findBySearchNameAndGroupEntity(String searchName, GroupEntity groupEntity);

    Optional<UserGroupEntity> findByGroupIdAndUserId(Long groupId, Long userId);

    List<UserGroupEntity> findByGroupId(Long groupId);

    List<CountUserGroupVO> countByGroupEntity(List<GroupEntity> groupEntityList);
}

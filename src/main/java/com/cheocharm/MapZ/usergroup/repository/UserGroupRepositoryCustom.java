package com.cheocharm.MapZ.usergroup.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.repository.vo.CountUserGroupVO;

import java.util.List;

public interface UserGroupRepositoryCustom {
    List<UserGroupEntity> fetchJoinByUserEntity(UserEntity userEntity);

    List<GroupEntity> getGroupEntityList(UserEntity userEntity);
    List<ChiefUserImageVO> findChiefUserImage(List<GroupEntity> groupEntityList);

    List<UserGroupEntity> findBySearchNameAndGroupEntity(String searchName, GroupEntity groupEntity);

    List<CountUserGroupVO> countByGroupEntity(List<GroupEntity> groupEntityList);
}

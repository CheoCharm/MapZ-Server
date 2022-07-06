package com.cheocharm.MapZ.usergroup.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;

import java.util.List;

public interface UserGroupRepositoryCustom {
    List<UserGroupEntity> fetchJoinByUserEntity(UserEntity userEntity);

    List<String> findUserImage(GroupEntity groupEntity);
}

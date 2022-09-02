package com.cheocharm.MapZ.usergroup.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface UserGroupRepositoryCustom {
    List<UserGroupEntity> fetchJoinByUserEntity(UserEntity userEntity);

    Slice<UserGroupEntity> fetchByUserEntityAndSearchNameAndOrderByUserName(UserEntity userEntity, String searchName, Pageable pageable);

    List<String> findUserImage(GroupEntity groupEntity);

    List<UserGroupEntity> findBySearchNameAndGroupEntity(String searchName, GroupEntity groupEntity);
}

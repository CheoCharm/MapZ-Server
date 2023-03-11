package com.cheocharm.MapZ.usergroup.domain.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.domain.UserGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroupEntity, Long>, UserGroupRepositoryCustom {
    Optional<UserGroupEntity> findByUserEntityAndGroupEntity(UserEntity userEntity, GroupEntity groupEntity);
}

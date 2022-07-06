package com.cheocharm.MapZ.usergroup.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupRepository extends JpaRepository<UserGroupEntity, Long>, UserGroupRepositoryCustom {

    int countByGroupEntity(GroupEntity groupEntity);

}

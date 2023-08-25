package com.cheocharm.MapZ.usergroup.domain.repository;

import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.usergroup.domain.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long>, UserGroupRepositoryCustom {
    Optional<UserGroup> findByUserAndGroup(User user, Group group);
}

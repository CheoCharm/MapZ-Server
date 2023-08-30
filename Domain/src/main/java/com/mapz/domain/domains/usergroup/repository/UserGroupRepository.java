package com.mapz.domain.domains.usergroup.repository;

import com.mapz.domain.domains.group.entity.Group;
import com.mapz.domain.domains.user.entity.User;
import com.mapz.domain.domains.usergroup.entity.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGroupRepository extends JpaRepository<UserGroup, Long>, UserGroupRepositoryCustom {
    Optional<UserGroup> findByUserAndGroup(User user, Group group);
}

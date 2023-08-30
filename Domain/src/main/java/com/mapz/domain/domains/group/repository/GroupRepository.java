package com.mapz.domain.domains.group.repository;

import com.mapz.domain.domains.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupRepositoryCustom {
    Optional<Group> findByGroupName(String groupName);

    boolean existsByGroupName(String groupName);
}

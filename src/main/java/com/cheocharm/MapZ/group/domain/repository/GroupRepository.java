package com.cheocharm.MapZ.group.domain.repository;

import com.cheocharm.MapZ.group.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long>, GroupRepositoryCustom {
    Optional<Group> findByGroupName(String groupName);

    boolean existsByGroupName(String groupName);
}

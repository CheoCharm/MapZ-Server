package com.cheocharm.MapZ.group.domain.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    Optional<GroupEntity> findByGroupName(String groupName);
}

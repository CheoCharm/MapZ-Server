package com.mapz.domain.domains.group.repository;

import com.mapz.domain.domains.group.entity.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface GroupRepositoryCustom {
    Slice<Group> findByGroupName(String searchName, Long cursorId, Pageable pageable);
}

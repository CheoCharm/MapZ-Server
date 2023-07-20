package com.cheocharm.MapZ.group.domain.repository;

import com.cheocharm.MapZ.group.domain.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface GroupRepositoryCustom {
    Slice<Group> findByGroupName(String searchName, Long cursorId, Pageable pageable);
}

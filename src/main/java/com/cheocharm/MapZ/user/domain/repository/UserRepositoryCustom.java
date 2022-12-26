package com.cheocharm.MapZ.user.domain.repository;

import com.cheocharm.MapZ.user.domain.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface UserRepositoryCustom {
    Slice<UserEntity> fetchByUserEntityAndSearchName(UserEntity userEntity, String searchName, Long cursorId, Pageable pageable);

    List<UserEntity> getUserEntityListByUserIdList(List<Long> userIdList);
}

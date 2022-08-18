package com.cheocharm.MapZ.user.domain.repository;

import com.cheocharm.MapZ.user.domain.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface UserRepositoryCustom {
    Slice<UserEntity> fetchByUserEntityAndSearchName(UserEntity userEntity, String searchName, Pageable pageable);
}

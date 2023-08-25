package com.cheocharm.MapZ.user.domain.repository;

import com.cheocharm.MapZ.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface UserRepositoryCustom {
    Slice<User> fetchByUserAndSearchName(User user, String searchName, Long cursorId, Pageable pageable);

    List<User> getUserListByUserIdList(List<Long> userIdList);
}

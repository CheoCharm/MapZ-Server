package com.cheocharm.MapZ.user.domain.repository;

import com.cheocharm.MapZ.user.domain.UserEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.cheocharm.MapZ.common.util.QuerydslSupport.*;
import static com.cheocharm.MapZ.user.domain.QUserEntity.*;

@RequiredArgsConstructor
@Component
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<UserEntity> fetchByUserEntityAndSearchName(UserEntity userCond, String searchName, Long cursorId, Pageable pageable) {
        JPAQuery<UserEntity> query = queryFactory
                .selectFrom(userEntity)
                .where(userEntity.username.contains(searchName)
                        .and(userNe(userCond))
                        .and(userEntity.id.lt(cursorId))
                );
        return fetchSliceByCursor(userEntity.getType(), userEntity.getMetadata(), query, pageable);
    }

    @Override
    public List<UserEntity> getUserEntityListByUserIdList(List<Long> userIdList) {
        return queryFactory
                .selectFrom(userEntity)
                .where(userEntity.id.in(userIdList))
                .fetch();
    }

    private BooleanExpression userNe(UserEntity userCond) {
        return userEntity.ne(userCond);
    }
}

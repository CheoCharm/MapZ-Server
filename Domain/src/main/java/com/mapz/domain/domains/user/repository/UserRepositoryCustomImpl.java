package com.mapz.domain.domains.user.repository;

import com.mapz.domain.domains.user.entity.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mapz.domain.common.util.QuerydslSupport.fetchSliceByCursor;
import static com.mapz.domain.domains.user.entity.QUser.user;

@RequiredArgsConstructor
@Component
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<User> fetchByUserAndSearchName(User userCond, String searchName, Long cursorId, Pageable pageable) {
        JPAQuery<User> query = queryFactory
                .selectFrom(user)
                .where(user.username.contains(searchName)
                        .and(userNe(userCond))
                        .and(user.id.lt(cursorId))
                );
        return fetchSliceByCursor(user.getType(), user.getMetadata(), query, pageable);
    }

    @Override
    public List<User> getUserListByUserIdList(List<Long> userIdList) {
        return queryFactory
                .selectFrom(user)
                .where(user.id.in(userIdList))
                .fetch();
    }

    private BooleanExpression userNe(User userCond) {
        return user.ne(userCond);
    }
}

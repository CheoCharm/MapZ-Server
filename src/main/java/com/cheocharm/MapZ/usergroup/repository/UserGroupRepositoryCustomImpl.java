package com.cheocharm.MapZ.usergroup.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.cheocharm.MapZ.common.util.QuerydslSupport.*;
import static com.cheocharm.MapZ.group.domain.QGroupEntity.*;
import static com.cheocharm.MapZ.user.domain.QUserEntity.*;
import static com.cheocharm.MapZ.usergroup.QUserGroupEntity.*;

@RequiredArgsConstructor
@Component
public class UserGroupRepositoryCustomImpl implements UserGroupRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserGroupEntity> fetchJoinByUserEntity(UserEntity userEntity) {
        return fetchJoinQuery()
                .where(userEq(userEntity))
                .fetch();
    }

    @Override
    public Slice<UserGroupEntity> fetchByUserEntityAndSearchNameAndOrderByUserName(UserEntity userEntity, String searchName, Pageable pageable) {
        JPAQuery<UserGroupEntity> query = fetchJoinQuery()
                .orderBy(userGroupEntity.groupEntity.groupName.asc())
                .where(userGroupEntity.groupEntity.groupName.contains(searchName)
                        .and(userEq(userEntity))
                );

        return fetchSlice(query, pageable);
    }

    @Override
    public List<String> findUserImage(GroupEntity groupEntity) {
        return queryFactory
                .select(userGroupEntity.userEntity.userImageUrl)
                .from(userGroupEntity)
                .where(groupEq(groupEntity))
                .limit(4)
                .fetch();
    }

    private JPAQuery<UserGroupEntity> fetchJoinQuery() {
        return queryFactory
                .selectFrom(userGroupEntity)
                .innerJoin(userGroupEntity.groupEntity, groupEntity)
                .innerJoin(userGroupEntity.userEntity, userEntity)
                .fetchJoin();
    }

    private BooleanExpression userEq(UserEntity userCond) {
        return userGroupEntity.userEntity.eq(userCond);
    }

    private BooleanExpression groupEq(GroupEntity groupCond) {
        return userGroupEntity.groupEntity.eq(groupCond);
    }
}

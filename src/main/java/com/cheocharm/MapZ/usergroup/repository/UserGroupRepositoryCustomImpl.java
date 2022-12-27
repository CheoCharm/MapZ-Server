package com.cheocharm.MapZ.usergroup.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.UserRole;
import com.cheocharm.MapZ.usergroup.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.repository.vo.CountUserGroupVO;
import com.cheocharm.MapZ.usergroup.repository.vo.QChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.repository.vo.QCountUserGroupVO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.cheocharm.MapZ.group.domain.QGroupEntity.*;
import static com.cheocharm.MapZ.user.domain.QUserEntity.*;
import static com.cheocharm.MapZ.usergroup.QUserGroupEntity.*;

@RequiredArgsConstructor
@Component
public class UserGroupRepositoryCustomImpl implements UserGroupRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GroupEntity> getGroupEntityList(UserEntity userEntity) {
        return queryFactory
                .select(userGroupEntity.groupEntity)
                .from(userGroupEntity)
                .where(userEq(userEntity))
                .fetch();
    }

    @Override
    public List<ChiefUserImageVO> findChiefUserImage(List<GroupEntity> groupEntityList) {
        return queryFactory
                .select(new QChiefUserImageVO(
                        userGroupEntity.userEntity.userImageUrl, userGroupEntity.id
                ))
                .from(userGroupEntity)
                .where(userGroupEntity.groupEntity.in(groupEntityList)
                        .and(userGroupEntity.userRole.eq(UserRole.CHIEF)))
                .fetch();
    }

    @Override
    public List<UserGroupEntity> findBySearchNameAndGroupEntity(String searchName, GroupEntity groupEntity) {
        return fetchJoinUserEntity()
                .where(userGroupEntity.userEntity.username.contains(searchName)
                        .and(groupEq(groupEntity))
                )
                .fetch();
    }

    @Override
    public Optional<UserGroupEntity> findByGroupIdAndUserId(Long groupId, Long userId) {
        return Optional.ofNullable(
                fetchJoinQuery()
                        .where(groupIdEq(groupId)
                                .and(userIdEq(userId))
                        )
                        .fetchOne()
        );
    }

    @Override
    public List<CountUserGroupVO> countByGroupEntity(List<GroupEntity> groupEntityList) {
        return queryFactory
                .select(new QCountUserGroupVO(
                        userGroupEntity.count(), userGroupEntity.groupEntity.id
                ))
                .from(userGroupEntity)
                .where(userGroupEntity.groupEntity.in(groupEntityList))
                .groupBy(userGroupEntity.groupEntity.id)
                .fetch();
    }

    private JPAQuery<UserGroupEntity> fetchJoinQuery() {
        return queryFactory
                .selectFrom(userGroupEntity)
                .innerJoin(userGroupEntity.groupEntity, groupEntity)
                .innerJoin(userGroupEntity.userEntity, userEntity)
                .fetchJoin();
    }

    private JPAQuery<UserGroupEntity> fetchJoinUserEntity() {
        return queryFactory
                .selectFrom(userGroupEntity)
                .innerJoin(userGroupEntity.userEntity, userEntity)
                .fetchJoin();
    }

    private JPAQuery<UserGroupEntity> fetchJoinGroupEntity() {
        return queryFactory
                .selectFrom(userGroupEntity)
                .innerJoin(userGroupEntity.groupEntity, groupEntity)
                .fetchJoin();
    }

    private BooleanExpression userEq(UserEntity userCond) {
        return userGroupEntity.userEntity.eq(userCond);
    }

    private BooleanExpression groupEq(GroupEntity groupCond) {
        return userGroupEntity.groupEntity.eq(groupCond);
    }

    private BooleanExpression userIdEq(Long userId) {
        return userGroupEntity.userEntity.id.eq(userId);
    }

    private BooleanExpression groupIdEq(Long groupId) {
        return userGroupEntity.groupEntity.id.eq(groupId);
    }
}

package com.cheocharm.MapZ.usergroup.domain.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import com.cheocharm.MapZ.usergroup.domain.UserGroupEntity;
import com.cheocharm.MapZ.usergroup.domain.UserRole;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.ChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.CountUserGroupVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.MyInvitationVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.QChiefUserImageVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.QCountUserGroupVO;
import com.cheocharm.MapZ.usergroup.domain.repository.vo.QMyInvitationVO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.cheocharm.MapZ.common.util.QuerydslSupport.fetchSliceByCursor;
import static com.cheocharm.MapZ.group.domain.QGroupEntity.groupEntity;
import static com.cheocharm.MapZ.user.domain.QUserEntity.userEntity;
import static com.cheocharm.MapZ.usergroup.domain.QUserGroupEntity.userGroupEntity;

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
    public List<UserGroupEntity> findBySearchNameAndGroupId(String searchName, Long groupId) {
        return fetchJoinUserEntity()
                .where(userGroupEntity.userEntity.username.contains(searchName)
                        .and(groupIdEq(groupId))
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
    public List<UserGroupEntity> findByGroupId(Long groupId) {
        return fetchJoinUserEntity()
                .where(groupIdEq(groupId))
                .fetch();
    }

    @Override
    public List<CountUserGroupVO> countByGroupEntity(List<GroupEntity> groupEntityList) {
        return queryFactory
                .select(new QCountUserGroupVO(
                        userGroupEntity.count(), userGroupEntity.groupEntity.id
                ))
                .from(userGroupEntity)
                .where(userGroupEntity.groupEntity.in(groupEntityList)
                        .and(userGroupEntity.invitationStatus.eq(InvitationStatus.ACCEPT))
                )
                .groupBy(userGroupEntity.groupEntity.id)
                .fetch();
    }

    @Override
    public Long countByGroupId(Long groupId) {
        return queryFactory
                .select(userGroupEntity.count())
                .from(userGroupEntity)
                .where(groupIdEq(groupId))
                .fetchOne();
    }

    @Override
    public Slice<MyInvitationVO> getInvitationSlice(Long userId, Long cursorId, Pageable pageable) {
        JPAQuery<MyInvitationVO> query = queryFactory
                .select(new QMyInvitationVO(
                        groupEntity.id,
                        groupEntity.groupName,
                        groupEntity.createdAt
                ))
                .from(userGroupEntity)
                .innerJoin(userGroupEntity.groupEntity, groupEntity)
                .where(userGroupEntity.id.lt(cursorId)
                        .and(userIdEq(userId))
                        .and(userGroupEntity.invitationStatus.eq(InvitationStatus.SEND))
                );

        return fetchSliceByCursor(userGroupEntity.getType(), userGroupEntity.getMetadata(), query, pageable);
    }

    @Override
    public List<Long> getGroupIdByUserId(Long userId) {
        return queryFactory
                .select(userGroupEntity.groupEntity.id)
                .from(userGroupEntity)
                .where(userIdEq(userId))
                .fetch();
    }

    private JPAQuery<UserGroupEntity> fetchJoinQuery() {
        return queryFactory
                .selectFrom(userGroupEntity)
                .innerJoin(userGroupEntity.groupEntity, groupEntity).fetchJoin()
                .innerJoin(userGroupEntity.userEntity, userEntity).fetchJoin();
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

package com.cheocharm.MapZ.usergroup.domain.repository;

import com.cheocharm.MapZ.group.domain.Group;
import com.cheocharm.MapZ.user.domain.User;
import com.cheocharm.MapZ.usergroup.domain.InvitationStatus;
import com.cheocharm.MapZ.usergroup.domain.UserGroup;
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
import static com.cheocharm.MapZ.group.domain.QGroup.group;
import static com.cheocharm.MapZ.user.domain.QUser.user;
import static com.cheocharm.MapZ.usergroup.domain.QUserGroup.userGroup;

@RequiredArgsConstructor
@Component
public class UserGroupRepositoryCustomImpl implements UserGroupRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Group> getGroups(User user) {
        return queryFactory
                .select(userGroup.group)
                .from(userGroup)
                .where(userEq(user))
                .fetch();
    }

    @Override
    public List<ChiefUserImageVO> findChiefUserImage(List<Group> groupList) {
        return queryFactory
                .select(new QChiefUserImageVO(
                        userGroup.user.userImageUrl, userGroup.id
                ))
                .from(userGroup)
                .where(userGroup.group.in(groupList)
                        .and(userGroup.userRole.eq(UserRole.CHIEF)))
                .fetch();
    }

    @Override
    public List<UserGroup> findBySearchNameAndGroupId(String searchName, Long groupId) {
        return fetchJoinUserEntity()
                .where(userGroup.user.username.contains(searchName)
                        .and(groupIdEq(groupId))
                )
                .fetch();
    }

    @Override
    public Optional<UserGroup> findByGroupIdAndUserId(Long groupId, Long userId) {
        return Optional.ofNullable(
                fetchJoinQuery()
                        .where(groupIdEq(groupId)
                                .and(userIdEq(userId))
                        )
                        .fetchOne()
        );
    }

    @Override
    public List<UserGroup> findByGroupId(Long groupId) {
        return fetchJoinUserEntity()
                .where(groupIdEq(groupId))
                .fetch();
    }

    @Override
    public List<CountUserGroupVO> countByGroup(List<Group> groupList) {
        return queryFactory
                .select(new QCountUserGroupVO(
                        userGroup.count(), userGroup.group.id
                ))
                .from(userGroup)
                .where(userGroup.group.in(groupList)
                        .and(userGroup.invitationStatus.eq(InvitationStatus.ACCEPT))
                )
                .groupBy(userGroup.group.id)
                .fetch();
    }

    @Override
    public Long countByGroupId(Long groupId) {
        return queryFactory
                .select(userGroup.count())
                .from(userGroup)
                .where(groupIdEq(groupId))
                .fetchOne();
    }

    @Override
    public Slice<MyInvitationVO> getInvitationSlice(Long userId, Long cursorId, Pageable pageable) {
        JPAQuery<MyInvitationVO> query = queryFactory
                .select(new QMyInvitationVO(
                        group.id,
                        group.groupName,
                        group.createdAt
                ))
                .from(userGroup)
                .innerJoin(userGroup.group, group)
                .where(userGroup.id.lt(cursorId)
                        .and(userIdEq(userId))
                        .and(userGroup.invitationStatus.eq(InvitationStatus.SEND))
                );

        return fetchSliceByCursor(userGroup.getType(), userGroup.getMetadata(), query, pageable);
    }

    @Override
    public List<Long> getGroupIdByUserId(Long userId) {
        return queryFactory
                .select(userGroup.group.id)
                .from(userGroup)
                .where(userIdEq(userId))
                .fetch();
    }

    private JPAQuery<UserGroup> fetchJoinQuery() {
        return queryFactory
                .selectFrom(userGroup)
                .innerJoin(userGroup.group, group).fetchJoin()
                .innerJoin(userGroup.user, user).fetchJoin();
    }

    private JPAQuery<UserGroup> fetchJoinUserEntity() {
        return queryFactory
                .selectFrom(userGroup)
                .innerJoin(userGroup.user, user)
                .fetchJoin();
    }

    private JPAQuery<UserGroup> fetchJoinGroupEntity() {
        return queryFactory
                .selectFrom(userGroup)
                .innerJoin(userGroup.group, group)
                .fetchJoin();
    }

    private BooleanExpression userEq(User userCond) {
        return userGroup.user.eq(userCond);
    }

    private BooleanExpression groupEq(Group groupCond) {
        return userGroup.group.eq(groupCond);
    }

    private BooleanExpression userIdEq(Long userId) {
        return userGroup.user.id.eq(userId);
    }

    private BooleanExpression groupIdEq(Long groupId) {
        return userGroup.group.id.eq(groupId);
    }
}

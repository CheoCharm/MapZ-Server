package com.mapz.domain.domains.group.repository;

import com.mapz.domain.domains.group.entity.Group;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import static com.mapz.domain.common.util.QuerydslSupport.fetchSliceByCursor;
import static com.mapz.domain.domains.group.entity.QGroup.group;


@RequiredArgsConstructor
public class GroupRepositoryCustomImpl implements GroupRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Group> findByGroupName(String groupName, Long cursorId, Pageable pageable) {
        JPAQuery<Group> query = queryFactory
                .selectFrom(group)
                .where(group.groupName.contains(groupName)
                        .and(group.openStatus.eq(true))
                        .and(group.id.lt(cursorId))
                );

        return fetchSliceByCursor(group.getType(), group.getMetadata(), query, pageable);
    }

}

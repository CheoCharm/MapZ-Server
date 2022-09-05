package com.cheocharm.MapZ.group.domain.repository;

import com.cheocharm.MapZ.group.domain.GroupEntity;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import static com.cheocharm.MapZ.common.util.QuerydslSupport.fetchSlice;
import static com.cheocharm.MapZ.group.domain.QGroupEntity.*;

@RequiredArgsConstructor
public class GroupRepositoryCustomImpl implements GroupRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<GroupEntity> findByGroupName(String groupName, Pageable pageable) {
        JPAQuery<GroupEntity> query = queryFactory
                .selectFrom(groupEntity)
                .where(groupEntity.groupName.contains(groupName)
                        .and(groupEntity.openStatus.eq(true))
                );

        return fetchSlice(groupEntity.getType(), groupEntity.getMetadata(), query, pageable);
    }

}

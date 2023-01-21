package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cheocharm.MapZ.diary.domain.QDiaryEntity.*;

@RequiredArgsConstructor
public class DiaryRepositoryCustomImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<DiaryEntity> findByUserIdAndGroupId(Long userId, Long groupId) {
        return queryFactory
                .selectFrom(diaryEntity)
                .where(userIdEq(userId)
                        .and(groupIdEq(groupId))
                )
                .fetch();
    }

    private BooleanExpression userIdEq(Long userId) {
        return diaryEntity.userEntity.id.eq(userId);
    }

    private BooleanExpression groupIdEq(Long groupId) {
        return diaryEntity.groupEntity.id.eq(groupId);
    }
}

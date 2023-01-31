package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryLikeEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cheocharm.MapZ.diary.domain.QDiaryLikeEntity.*;
import static com.cheocharm.MapZ.user.domain.QUserEntity.*;

@RequiredArgsConstructor
public class DiaryLikeRepositoryCustomImpl implements DiaryLikeRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<DiaryLikeEntity> findByDiaryId(Long diaryId) {
        return queryFactory
                .selectFrom(diaryLikeEntity)
                .innerJoin(diaryLikeEntity.userEntity, userEntity)
                .fetchJoin()
                .where(diaryIdEq(diaryId))
                .fetch();
    }

    private BooleanExpression diaryIdEq(Long diaryId) {
        return diaryLikeEntity.diaryEntity.id.eq(diaryId);
    }
}

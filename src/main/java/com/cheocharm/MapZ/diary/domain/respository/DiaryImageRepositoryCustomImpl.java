package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cheocharm.MapZ.diary.domain.QDiaryImageEntity.diaryImageEntity;

@RequiredArgsConstructor
public class DiaryImageRepositoryCustomImpl implements DiaryImageRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    @Override
    public List<String> findAllByDiaryId(Long diaryId) {
        return queryFactory
                .select(diaryImageEntity.diaryImageUrl)
                .from(diaryImageEntity)
                .where(diaryIdEq(diaryId))
                .fetch();
    }

    @Override
    public void deleteAllByDiaryId(Long diaryId) {
        queryFactory
                .delete(diaryImageEntity)
                .where(diaryIdEq(diaryId))
                .execute();
    }

    @Override
    public void deleteAllByDiaryEntityList(List<DiaryEntity> diaryEntities) {
        queryFactory
                .delete(diaryImageEntity)
                .where(diaryImageEntity.diaryEntity.in(diaryEntities))
                .execute();
    }

    private BooleanExpression diaryIdEq(Long diaryId) {
        return diaryImageEntity.diaryEntity.id.eq(diaryId);
    }
}

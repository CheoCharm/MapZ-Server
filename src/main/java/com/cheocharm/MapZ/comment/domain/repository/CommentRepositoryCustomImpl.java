package com.cheocharm.MapZ.comment.domain.repository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cheocharm.MapZ.comment.domain.QCommentEntity.*;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteAllByDiaryEntityList(List<DiaryEntity> diaryEntityList) {
        queryFactory
                .delete(commentEntity)
                .where(commentEntity.diaryEntity.in(diaryEntityList))
                .execute();
    }
}

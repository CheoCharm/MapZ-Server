package com.cheocharm.MapZ.like.domain.repository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.like.domain.DiaryLikeEntity;
import com.cheocharm.MapZ.diary.domain.respository.vo.MyLikeDiaryVO;
import com.cheocharm.MapZ.diary.domain.respository.vo.QMyLikeDiaryVO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static com.cheocharm.MapZ.comment.domain.QCommentEntity.commentEntity;
import static com.cheocharm.MapZ.common.util.QuerydslSupport.fetchSliceByCursor;
import static com.cheocharm.MapZ.diary.domain.QDiaryEntity.diaryEntity;
import static com.cheocharm.MapZ.group.domain.QGroupEntity.groupEntity;
import static com.cheocharm.MapZ.like.domain.QDiaryLikeEntity.diaryLikeEntity;
import static com.cheocharm.MapZ.user.domain.QUserEntity.userEntity;

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

    @Override
    public Slice<MyLikeDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable) {

        JPAQuery<MyLikeDiaryVO> query = queryFactory
                .select(new QMyLikeDiaryVO(
                        diaryEntity.title,
                        diaryEntity.createdAt,
                        diaryEntity.content, //일기 대표이미지로 바꿔야 할 부분(임시로 사용)
                        groupEntity.id,
                        diaryEntity.id,
                        commentEntity.count()
                ))
                .from(diaryLikeEntity)
                .innerJoin(diaryLikeEntity.diaryEntity, diaryEntity)
                .innerJoin(diaryEntity.groupEntity, groupEntity)
                .leftJoin(diaryEntity.commentEntities, commentEntity)
                .where(userIdEq(userId))
                .where(diaryEntity.id.lt(cursorId))
                .groupBy(diaryEntity.id);

        return fetchSliceByCursor(diaryEntity.getType(), diaryEntity.getMetadata(), query, pageable);
    }

    @Override
    public void deleteAllByDiaryEntityList(List<DiaryEntity> diaryEntityList) {
        queryFactory
                .delete(diaryLikeEntity)
                .where(diaryLikeEntity.diaryEntity.in(diaryEntityList))
                .execute();
    }

    private BooleanExpression diaryIdEq(Long diaryId) {
        return diaryLikeEntity.diaryEntity.id.eq(diaryId);
    }

    private BooleanExpression userIdEq(Long userId) {
        return diaryLikeEntity.userEntity.id.eq(userId);
    }
}

package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.respository.vo.DiaryDetailVO;
import com.cheocharm.MapZ.diary.domain.respository.vo.DiarySliceVO;
import com.cheocharm.MapZ.diary.domain.respository.vo.MyDiaryVO;
import com.cheocharm.MapZ.diary.domain.respository.vo.QDiaryDetailVO;
import com.cheocharm.MapZ.diary.domain.respository.vo.QDiarySliceVO;
import com.cheocharm.MapZ.diary.domain.respository.vo.QMyDiaryVO;
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
public class DiaryRepositoryCustomImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<DiarySliceVO> getDiarySlice(Long userId, Long groupId, Long cursorId, Pageable pageable) {
        final JPAQuery<DiarySliceVO> query = queryFactory
                .select(new QDiarySliceVO(
                        diaryEntity.id,
                        diaryEntity.title,
                        diaryEntity.content,
                        diaryEntity.address,
                        diaryEntity.createdAt,
                        userEntity.username,
                        userEntity.userImageUrl,
                        diaryEntity.diaryLikeEntities.size(),
                        diaryLikeEntity.isNotNull(),
                        commentEntity.count(),
                        userEntity.id.eq(userId)
                ))
                .from(diaryEntity)
                .leftJoin(diaryEntity.diaryLikeEntities, diaryLikeEntity)
                .on(likeUserIdEq(userId))
                .innerJoin(diaryEntity.userEntity, userEntity)
                .leftJoin(diaryEntity.commentEntities, commentEntity)
                .where(groupIdEq(groupId)
                        .and(diaryIdLt(cursorId))
                )
                .groupBy(diaryEntity.id, diaryLikeEntity.id); //dairyLikeEntity는 nonaggregated column때문에 작성

        return fetchSliceByCursor(diaryEntity.getType(), diaryEntity.getMetadata(), query, pageable);
    }

    @Override
    public Slice<MyDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable) {
        JPAQuery<MyDiaryVO> query = queryFactory
                .select(new QMyDiaryVO(
                        diaryEntity.title,
                        diaryEntity.createdAt,
                        diaryEntity.content, //일기 대표이미지로 바꿔야 할 부분(임시로 사용)
                        groupEntity.id,
                        diaryEntity.id,
                        commentEntity.count()
                ))
                .from(diaryEntity)
                .innerJoin(diaryEntity.groupEntity, groupEntity)
                .leftJoin(diaryEntity.commentEntities, commentEntity)
                .where(userIdEq(userId)
                        .and(diaryIdLt(cursorId))
                )
                .groupBy(diaryEntity.id);

        return fetchSliceByCursor(diaryEntity.getType(), diaryEntity.getMetadata(), query, pageable);
    }

    private BooleanExpression diaryIdLt(Long cursorId) {
        return diaryEntity.id.lt(cursorId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        queryFactory
                .delete(diaryEntity)
                .where(userIdEq(userId))
                .execute();
    }

    @Override
    public List<DiaryEntity> findAllByUserId(Long userId) {
        return queryFactory
                .selectFrom(diaryEntity)
                .where(userIdEq(userId))
                .fetch();
    }

    @Override
    public DiaryDetailVO getDiaryDetail(Long diaryId, Long userId) {
        return queryFactory
                .select(new QDiaryDetailVO(
                        diaryEntity.title,
                        diaryEntity.content,
                        diaryEntity.address,
                        diaryEntity.createdAt,
                        userEntity.username,
                        userEntity.userImageUrl,
                        diaryEntity.diaryLikeEntities.size(),
                        diaryLikeEntity.isNotNull(),
                        commentEntity.count(),
                        userEntity.id.eq(userId)
                ))
                .from(diaryEntity)
                .leftJoin(diaryEntity.diaryLikeEntities, diaryLikeEntity)
                .on(likeUserIdEq(userId))
                .innerJoin(diaryEntity.userEntity, userEntity)
                .leftJoin(diaryEntity.commentEntities, commentEntity)
                .where(diaryIdEq(diaryId))
                .groupBy(diaryLikeEntity.id) // nonaggregated Column 때문에 작성
                .fetchOne();
    }

    private BooleanExpression likeUserIdEq(Long userId) {
        return diaryLikeEntity.userEntity.id.eq(userId);
    }

    private BooleanExpression userIdEq(Long userId) {
        return diaryEntity.userEntity.id.eq(userId);
    }

    private BooleanExpression groupIdEq(Long groupId) {
        return diaryEntity.groupEntity.id.eq(groupId);
    }

    private BooleanExpression diaryIdEq(Long diaryId) {
        return diaryEntity.id.eq(diaryId);
    }
}

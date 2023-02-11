package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.respository.vo.MyDiaryVO;
import com.cheocharm.MapZ.diary.domain.respository.vo.QMyDiaryVO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static com.cheocharm.MapZ.comment.domain.QCommentEntity.*;
import static com.cheocharm.MapZ.common.util.QuerydslSupport.*;
import static com.cheocharm.MapZ.diary.domain.QDiaryEntity.*;
import static com.cheocharm.MapZ.group.domain.QGroupEntity.*;

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
                .leftJoin(diaryEntity.commentEntityList, commentEntity)
                .where(userIdEq(userId))
                .where(diaryEntity.id.lt(cursorId))
                .groupBy(diaryEntity.id);

        return fetchSliceByCursor(diaryEntity.getType(), diaryEntity.getMetadata(), query, pageable);
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

    private BooleanExpression userIdEq(Long userId) {
        return diaryEntity.userEntity.id.eq(userId);
    }

    private BooleanExpression groupIdEq(Long groupId) {
        return diaryEntity.groupEntity.id.eq(groupId);
    }
}

package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryDetailVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiarySliceVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.MyDiaryVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.QDiaryDetailVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.QDiarySliceVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.QMyDiaryVO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static com.cheocharm.MapZ.comment.domain.QComment.comment;
import static com.cheocharm.MapZ.common.util.QuerydslSupport.fetchSliceByCursor;
import static com.cheocharm.MapZ.diary.domain.QDiary.diary;
import static com.cheocharm.MapZ.group.domain.QGroup.group;
import static com.cheocharm.MapZ.like.domain.QDiaryLike.diaryLike;
import static com.cheocharm.MapZ.user.domain.QUser.user;

@RequiredArgsConstructor
public class DiaryRepositoryCustomImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<DiarySliceVO> getDiarySlice(Long userId, Long groupId, Long cursorId, Pageable pageable) {
        final JPAQuery<DiarySliceVO> query = queryFactory
                .select(new QDiarySliceVO(
                        diary.id,
                        diary.title,
                        diary.content,
                        diary.address,
                        diary.createdAt,
                        user.username,
                        user.userImageUrl,
                        diary.diaryLikes.size(),
                        diaryLike.isNotNull(),
                        comment.count(),
                        user.id.eq(userId)
                ))
                .from(diary)
                .leftJoin(diary.diaryLikes, diaryLike)
                .on(likeUserIdEq(userId))
                .innerJoin(diary.user, user)
                .leftJoin(diary.comments, comment)
                .where(groupIdEq(groupId)
                        .and(diaryIdLt(cursorId))
                )
                .groupBy(diary.id, diaryLike.id); //dairyLikeEntity는 nonaggregated column때문에 작성

        return fetchSliceByCursor(diary.getType(), diary.getMetadata(), query, pageable);
    }

    @Override
    public Slice<MyDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable) {
        JPAQuery<MyDiaryVO> query = queryFactory
                .select(new QMyDiaryVO(
                        diary.title,
                        diary.createdAt,
                        diary.content, //일기 대표이미지로 바꿔야 할 부분(임시로 사용)
                        group.id,
                        diary.id,
                        comment.count()
                ))
                .from(diary)
                .innerJoin(diary.group, group)
                .leftJoin(diary.comments, comment)
                .where(userIdEq(userId)
                        .and(diaryIdLt(cursorId))
                )
                .groupBy(diary.id);

        return fetchSliceByCursor(diary.getType(), diary.getMetadata(), query, pageable);
    }

    private BooleanExpression diaryIdLt(Long cursorId) {
        return diary.id.lt(cursorId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        queryFactory
                .delete(diary)
                .where(userIdEq(userId))
                .execute();
    }

    @Override
    public List<Diary> findAllByUserId(Long userId) {
        return queryFactory
                .selectFrom(diary)
                .where(userIdEq(userId))
                .fetch();
    }

    @Override
    public DiaryDetailVO getDiaryDetail(Long diaryId, Long userId) {
        return queryFactory
                .select(new QDiaryDetailVO(
                        diary.title,
                        diary.content,
                        diary.address,
                        diary.createdAt,
                        user.username,
                        user.userImageUrl,
                        diary.diaryLikes.size(),
                        diaryLike.isNotNull(),
                        comment.count(),
                        user.id.eq(userId)
                ))
                .from(diary)
                .leftJoin(diary.diaryLikes, diaryLike)
                .on(likeUserIdEq(userId))
                .innerJoin(diary.user, user)
                .leftJoin(diary.comments, comment)
                .where(diaryIdEq(diaryId))
                .groupBy(diaryLike.id) // nonaggregated Column 때문에 작성
                .fetchOne();
    }

    private BooleanExpression likeUserIdEq(Long userId) {
        return diaryLike.user.id.eq(userId);
    }

    private BooleanExpression userIdEq(Long userId) {
        return diary.user.id.eq(userId);
    }

    private BooleanExpression groupIdEq(Long groupId) {
        return diary.group.id.eq(groupId);
    }

    private BooleanExpression diaryIdEq(Long diaryId) {
        return diary.id.eq(diaryId);
    }
}

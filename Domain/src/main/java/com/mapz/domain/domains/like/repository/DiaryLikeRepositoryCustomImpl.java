package com.mapz.domain.domains.like.repository;

import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.vo.QMyLikeDiaryVO;
import com.mapz.domain.domains.like.entity.DiaryLike;
import com.mapz.domain.domains.diary.vo.MyLikeDiaryVO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static com.mapz.domain.common.util.QuerydslSupport.fetchSliceByCursor;
import static com.mapz.domain.domains.comment.entity.QComment.comment;
import static com.mapz.domain.domains.diary.entity.QDiary.diary;
import static com.mapz.domain.domains.group.entity.QGroup.group;
import static com.mapz.domain.domains.like.entity.QDiaryLike.diaryLike;
import static com.mapz.domain.domains.user.entity.QUser.user;


@RequiredArgsConstructor
public class DiaryLikeRepositoryCustomImpl implements DiaryLikeRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<DiaryLike> findByDiaryId(Long diaryId) {
        return queryFactory
                .selectFrom(diaryLike)
                .innerJoin(diaryLike.user, user)
                .fetchJoin()
                .where(diaryIdEq(diaryId))
                .fetch();
    }

    @Override
    public Slice<MyLikeDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable) {

        JPAQuery<MyLikeDiaryVO> query = queryFactory
                .select(new QMyLikeDiaryVO(
                        diary.title,
                        diary.createdAt,
                        diary.content, //일기 대표이미지로 바꿔야 할 부분(임시로 사용)
                        group.id,
                        diary.id,
                        comment.count()
                ))
                .from(diaryLike)
                .innerJoin(diaryLike.diary, diary)
                .innerJoin(diary.group, group)
                .leftJoin(diary.comments, comment)
                .where(userIdEq(userId))
                .where(diary.id.lt(cursorId))
                .groupBy(diary.id);

        return fetchSliceByCursor(diary.getType(), diary.getMetadata(), query, pageable);
    }

    @Override
    public void deleteAllByDiaries(List<Diary> diaries) {
        queryFactory
                .delete(diaryLike)
                .where(diaryLike.diary.in(diaries))
                .execute();
    }

    private BooleanExpression diaryIdEq(Long diaryId) {
        return diaryLike.diary.id.eq(diaryId);
    }

    private BooleanExpression userIdEq(Long userId) {
        return diaryLike.user.id.eq(userId);
    }
}

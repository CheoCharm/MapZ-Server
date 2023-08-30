package com.mapz.domain.domains.comment.repository;

import com.mapz.domain.domains.comment.vo.CommentVO;
import com.mapz.domain.domains.comment.vo.QCommentVO;
import com.mapz.domain.domains.diary.entity.Diary;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static com.mapz.domain.common.util.QuerydslSupport.fetchSliceByCursor;
import static com.mapz.domain.domains.comment.entity.QComment.comment;
import static com.mapz.domain.domains.diary.entity.QDiary.diary;
import static com.mapz.domain.domains.user.entity.QUser.user;

@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteAllByDiaries(List<Diary> diaries) {
        queryFactory
                .delete(comment)
                .where(comment.diary.in(diaries))
                .execute();
    }

    @Override
    public Slice<CommentVO> findByDiaryId(Long userId, Long diaryId, Long cursorId, Pageable pageable) {
        JPAQuery<CommentVO> query = queryFactory
                .select(new QCommentVO(
                        comment.user.userImageUrl,
                        comment.user.username,
                        comment.createdAt,
                        comment.content,
                        comment.user.id,
                        comment.id,
                        comment.parentId,
                        user.id.eq(diary.user.id),
                        user.id.eq(userId)
                ))
                .from(comment)
                .innerJoin(comment.diary, diary)
                .innerJoin(comment.user, user)
                .where(comment.id.gt(cursorId));

        return fetchSliceByCursor(comment.getType(), comment.getMetadata(), query, pageable);
    }

}

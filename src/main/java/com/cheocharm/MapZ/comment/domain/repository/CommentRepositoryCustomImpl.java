package com.cheocharm.MapZ.comment.domain.repository;

import com.cheocharm.MapZ.comment.domain.repository.vo.CommentVO;
import com.cheocharm.MapZ.comment.domain.repository.vo.QCommentVO;
import com.cheocharm.MapZ.diary.domain.Diary;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static com.cheocharm.MapZ.comment.domain.QComment.comment;
import static com.cheocharm.MapZ.common.util.QuerydslSupport.fetchSliceByCursor;
import static com.cheocharm.MapZ.diary.domain.QDiary.diary;
import static com.cheocharm.MapZ.user.domain.QUser.user;

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

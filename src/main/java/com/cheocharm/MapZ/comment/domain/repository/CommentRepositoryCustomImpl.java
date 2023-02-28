package com.cheocharm.MapZ.comment.domain.repository;

import com.cheocharm.MapZ.comment.domain.repository.vo.CommentVO;
import com.cheocharm.MapZ.comment.domain.repository.vo.QCommentVO;
import com.cheocharm.MapZ.common.interceptor.UserThreadLocal;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.user.domain.UserEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static com.cheocharm.MapZ.comment.domain.QCommentEntity.*;
import static com.cheocharm.MapZ.common.util.QuerydslSupport.fetchSliceByCursor;

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

    @Override
    public Slice<CommentVO> findByDiaryId(Long diaryId, Long cursorId, Pageable pageable) {
        UserEntity userEntity = UserThreadLocal.get();
        JPAQuery<CommentVO> query = queryFactory
                .select(new QCommentVO(
                        commentEntity.userEntity.userImageUrl,
                        commentEntity.userEntity.username,
                        commentEntity.createdAt,
                        commentEntity.content,
                        commentEntity.userEntity.id,
                        commentEntity.id,
                        commentEntity.parentId,
                        writerIdEq(commentEntity.diaryEntity.userEntity.id),
                        commenterIdEq(userEntity.getId())

                ))
                .from(commentEntity)
                .where(commentEntity.id.gt(cursorId));

        return fetchSliceByCursor(commentEntity.getType(), commentEntity.getMetadata(), query, pageable);
    }

    private BooleanExpression writerIdEq(NumberPath diaryUserId) {
        return commentEntity.userEntity.id.eq(diaryUserId);
    }

    private BooleanExpression commenterIdEq(Long userId) {
        return commentEntity.userEntity.id.eq(userId);
    }
}

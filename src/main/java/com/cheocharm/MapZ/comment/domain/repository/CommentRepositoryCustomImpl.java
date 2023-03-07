package com.cheocharm.MapZ.comment.domain.repository;

import com.cheocharm.MapZ.comment.domain.repository.vo.CommentVO;
import com.cheocharm.MapZ.comment.domain.repository.vo.QCommentVO;
import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static com.cheocharm.MapZ.comment.domain.QCommentEntity.*;
import static com.cheocharm.MapZ.common.util.QuerydslSupport.fetchSliceByCursor;
import static com.cheocharm.MapZ.diary.domain.QDiaryEntity.diaryEntity;
import static com.cheocharm.MapZ.user.domain.QUserEntity.userEntity;

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
    public Slice<CommentVO> findByDiaryId(Long userId, Long diaryId, Long cursorId, Pageable pageable) {
        JPAQuery<CommentVO> query = queryFactory
                .select(new QCommentVO(
                        commentEntity.userEntity.userImageUrl,
                        commentEntity.userEntity.username,
                        commentEntity.createdAt,
                        commentEntity.content,
                        commentEntity.userEntity.id,
                        commentEntity.id,
                        commentEntity.parentId,
                        userEntity.id.eq(diaryEntity.userEntity.id),
                        userEntity.id.eq(userId)
                ))
                .from(commentEntity)
                .innerJoin(commentEntity.diaryEntity, diaryEntity)
                .innerJoin(commentEntity.userEntity, userEntity)
                .where(commentEntity.id.gt(cursorId));

        return fetchSliceByCursor(commentEntity.getType(), commentEntity.getMetadata(), query, pageable);
    }

}

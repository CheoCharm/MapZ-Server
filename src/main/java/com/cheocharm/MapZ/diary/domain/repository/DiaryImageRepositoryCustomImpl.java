package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.DiaryImage;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryImagePreviewVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryPreviewVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.QDiaryImagePreviewVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.QDiaryPreviewVO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.cheocharm.MapZ.diary.domain.QDiary.diary;
import static com.cheocharm.MapZ.diary.domain.QDiaryImage.diaryImage;
import static com.cheocharm.MapZ.like.domain.QDiaryLike.diaryLike;

@RequiredArgsConstructor
public class DiaryImageRepositoryCustomImpl implements DiaryImageRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    @Override
    public List<DiaryImage> findAllByDiaryId(Long diaryId) {
        return queryFactory
                .select(diaryImage)
                .from(diaryImage)
                .where(diaryIdEq(diaryId))
                .fetch();
    }

    @Override
    public void deleteAllByDiaryId(Long diaryId) {
        queryFactory
                .delete(diaryImage)
                .where(diaryIdEq(diaryId))
                .execute();
    }

    @Override
    public void deleteAllByDiaries(List<Diary> diaries) {
        queryFactory
                .delete(diaryImage)
                .where(diaryImage.diary.in(diaries))
                .execute();
    }

    @Override
    public List<DiaryImagePreviewVO> findPreviewImage(List<Long> diaryIds) {
        return queryFactory
                .select(new QDiaryImagePreviewVO(
                        diaryImage.diary.id,
                        diaryImage.diaryImageUrl
                ))
                .from(diaryImage)
                .where(diaryImage.diary.id.in(diaryIds).and(diaryImage.imageOrder.eq(1)))
                .fetch();
    }

    public List<DiaryPreviewVO> getDiaryPreview(Long diaryId, Long userId) {
        return queryFactory
                .select(new QDiaryPreviewVO(
                        diaryImage.diaryImageUrl,
                        diaryImage.imageOrder,
                        diary.address,
                        diaryLike.isNotNull()
                ))
                .from(diaryImage)
                .innerJoin(diaryImage.diary, diary)
                .leftJoin(diary.diaryLikes, diaryLike)
                .on(likeUserIdEq(userId))
                .where(diaryIdEq(diaryId))
                .fetch();
    }

    private BooleanExpression diaryIdEq(Long diaryId) {
        return diaryImage.diary.id.eq(diaryId);
    }

    private BooleanExpression likeUserIdEq(Long userId) {
        return diaryLike.user.id.eq(userId);
    }
}

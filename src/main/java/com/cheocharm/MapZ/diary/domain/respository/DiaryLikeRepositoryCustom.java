package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryLikeEntity;

import java.util.List;

public interface DiaryLikeRepositoryCustom {
    List<DiaryLikeEntity> findByDiaryId(Long diaryId);
}

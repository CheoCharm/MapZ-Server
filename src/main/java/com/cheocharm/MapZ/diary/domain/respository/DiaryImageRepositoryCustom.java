package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;

import java.util.List;

public interface DiaryImageRepositoryCustom {
    List<String> findAllByDiaryId(Long diaryId);

    void deleteAllByDiaryId(Long diaryId);

    void deleteAllByDiaryEntityList(List<DiaryEntity> diaryEntities);
}

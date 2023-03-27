package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryImagePreviewVO;

import java.util.List;

public interface DiaryImageRepositoryCustom {
    List<String> findAllByDiaryId(Long diaryId);

    void deleteAllByDiaryId(Long diaryId);

    void deleteAllByDiaryEntityList(List<DiaryEntity> diaryEntities);

    List<DiaryImagePreviewVO> findPreviewImage(List<Long> diaryIds);
}

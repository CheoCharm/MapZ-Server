package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryImagePreviewVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryPreviewVO;

import java.util.List;

public interface DiaryImageRepositoryCustom {
    List<String> findAllByDiaryId(Long diaryId);

    void deleteAllByDiaryId(Long diaryId);

    void deleteAllByDiaries(List<Diary> diaryEntities);

    List<DiaryImagePreviewVO> findPreviewImage(List<Long> diaryIds);

    List<DiaryPreviewVO> getDiaryPreview(Long diaryId, Long userId);
}

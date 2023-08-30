package com.mapz.domain.domains.diary.repository;

import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.entity.DiaryImage;
import com.mapz.domain.domains.diary.vo.DiaryImagePreviewVO;
import com.mapz.domain.domains.diary.vo.DiaryPreviewVO;

import java.util.List;

public interface DiaryImageRepositoryCustom {
    List<DiaryImage> findAllByDiaryId(Long diaryId);

    void deleteAllByDiaryId(Long diaryId);

    void deleteAllByDiaries(List<Diary> diaryEntities);

    List<DiaryImagePreviewVO> findPreviewImage(List<Long> diaryIds);

    List<DiaryPreviewVO> getDiaryPreview(Long diaryId, Long userId);
}

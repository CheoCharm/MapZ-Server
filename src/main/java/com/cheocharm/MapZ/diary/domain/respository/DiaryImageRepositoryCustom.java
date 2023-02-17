package com.cheocharm.MapZ.diary.domain.respository;

import java.util.List;

public interface DiaryImageRepositoryCustom {
    List<String> findAllByDiaryId(Long diaryId);

    void deleteAllByDiaryId(Long diaryId);
}

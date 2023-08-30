package com.mapz.domain.domains.diary.repository;

import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.diary.vo.DiaryDetailVO;
import com.mapz.domain.domains.diary.vo.DiarySliceVO;
import com.mapz.domain.domains.diary.vo.MyDiaryVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface DiaryRepositoryCustom {
    Slice<DiarySliceVO> getDiarySlice(Long userId, Long groupId, Long cursorId, Pageable pageable);

    Slice<MyDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable);

    void deleteAllByUserId(Long userId);

    List<Diary> findAllByUserId(Long userId);

    DiaryDetailVO getDiaryDetail(Long diaryId, Long userId);
}

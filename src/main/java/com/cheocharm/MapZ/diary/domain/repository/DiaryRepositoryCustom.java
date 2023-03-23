package com.cheocharm.MapZ.diary.domain.repository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiaryDetailVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.DiarySliceVO;
import com.cheocharm.MapZ.diary.domain.repository.vo.MyDiaryVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface DiaryRepositoryCustom {
    Slice<DiarySliceVO> getDiarySlice(Long userId, Long groupId, Long cursorId, Pageable pageable);

    Slice<MyDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable);

    void deleteAllByUserId(Long userId);

    List<DiaryEntity> findAllByUserId(Long userId);

    DiaryDetailVO getDiaryDetail(Long diaryId, Long userId);
}

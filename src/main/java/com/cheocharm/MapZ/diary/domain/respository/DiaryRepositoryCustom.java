package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.diary.domain.respository.vo.MyDiaryVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface DiaryRepositoryCustom {
    List<DiaryEntity> findByUserIdAndGroupId(Long userId, Long groupId);

    Slice<MyDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable);

    void deleteAllByUserId(Long userId);

    List<DiaryEntity> findAllByUserId(Long userId);
}

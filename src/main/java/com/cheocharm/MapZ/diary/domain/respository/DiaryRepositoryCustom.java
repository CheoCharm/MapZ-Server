package com.cheocharm.MapZ.diary.domain.respository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;

import java.util.List;

public interface DiaryRepositoryCustom {
    List<DiaryEntity> findByUserIdAndGroupId(Long userId, Long groupId);
}

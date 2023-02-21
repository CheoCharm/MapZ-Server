package com.cheocharm.MapZ.like.domain.repository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;
import com.cheocharm.MapZ.like.domain.DiaryLikeEntity;
import com.cheocharm.MapZ.diary.domain.respository.vo.MyLikeDiaryVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface DiaryLikeRepositoryCustom {
    List<DiaryLikeEntity> findByDiaryId(Long diaryId);

    Slice<MyLikeDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable);

    void deleteAllByDiaryEntityList(List<DiaryEntity> diaryEntityList);
}

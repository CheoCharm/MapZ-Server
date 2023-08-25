package com.cheocharm.MapZ.like.domain.repository;

import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.like.domain.DiaryLike;
import com.cheocharm.MapZ.diary.domain.repository.vo.MyLikeDiaryVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface DiaryLikeRepositoryCustom {
    List<DiaryLike> findByDiaryId(Long diaryId);

    Slice<MyLikeDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable);

    void deleteAllByDiaries(List<Diary> diaries);
}

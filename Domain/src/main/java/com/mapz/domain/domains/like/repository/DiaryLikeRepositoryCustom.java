package com.mapz.domain.domains.like.repository;

import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.like.entity.DiaryLike;
import com.mapz.domain.domains.diary.vo.MyLikeDiaryVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface DiaryLikeRepositoryCustom {
    List<DiaryLike> findByDiaryId(Long diaryId);

    Slice<MyLikeDiaryVO> findByUserId(Long userId, Long cursorId, Pageable pageable);

    void deleteAllByDiaries(List<Diary> diaries);
}

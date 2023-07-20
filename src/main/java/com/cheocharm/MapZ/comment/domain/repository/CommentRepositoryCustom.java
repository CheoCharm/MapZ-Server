package com.cheocharm.MapZ.comment.domain.repository;

import com.cheocharm.MapZ.comment.domain.repository.vo.CommentVO;
import com.cheocharm.MapZ.diary.domain.Diary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface CommentRepositoryCustom {
    void deleteAllByDiaries(List<Diary> diaries);

    Slice<CommentVO> findByDiaryId(Long userId, Long diaryId, Long cursorId, Pageable pageable);
}

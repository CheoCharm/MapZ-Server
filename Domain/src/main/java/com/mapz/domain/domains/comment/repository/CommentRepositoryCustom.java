package com.mapz.domain.domains.comment.repository;

import com.mapz.domain.domains.comment.vo.CommentVO;
import com.mapz.domain.domains.diary.entity.Diary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface CommentRepositoryCustom {
    void deleteAllByDiaries(List<Diary> diaries);

    Slice<CommentVO> findByDiaryId(Long userId, Long diaryId, Long cursorId, Pageable pageable);
}

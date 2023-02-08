package com.cheocharm.MapZ.comment.domain.repository;

import com.cheocharm.MapZ.diary.domain.DiaryEntity;

import java.util.List;

public interface CommentRepositoryCustom {
    void deleteAllByDiaryEntityList(List<DiaryEntity> diaryEntityList);
}

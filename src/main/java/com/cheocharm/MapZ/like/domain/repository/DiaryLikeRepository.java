package com.cheocharm.MapZ.like.domain.repository;

import com.cheocharm.MapZ.diary.domain.Diary;
import com.cheocharm.MapZ.like.domain.DiaryLike;
import com.cheocharm.MapZ.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long>, DiaryLikeRepositoryCustom {
    Optional<DiaryLike> findByDiaryAndUser(Diary diary, User user);
}

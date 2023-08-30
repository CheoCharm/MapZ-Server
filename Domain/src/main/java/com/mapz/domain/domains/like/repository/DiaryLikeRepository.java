package com.mapz.domain.domains.like.repository;

import com.mapz.domain.domains.diary.entity.Diary;
import com.mapz.domain.domains.like.entity.DiaryLike;
import com.mapz.domain.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long>, DiaryLikeRepositoryCustom {
    Optional<DiaryLike> findByDiaryAndUser(Diary diary, User user);
}
